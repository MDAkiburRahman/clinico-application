package com.example.clinico.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Adapter.ImagePreviewAdapter;
import com.example.clinico.Domain.PrescriptionFolderModel;
import com.example.clinico.Domain.PrescriptionModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityPrescriptionFolderDetailBinding;
import com.example.clinico.databinding.ViewholderPrescriptionBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrescriptionFolderDetailActivity extends BaseActivity {
    private ActivityPrescriptionFolderDetailBinding binding;
    private PrescriptionFolderModel folder;
    private List<PrescriptionModel> prescriptionList = new ArrayList<>();
    private PrescriptionAdapter adapter;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
            new ActivityResultContracts.PickMultipleVisualMedia(10),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    showPreviewDialog(uris);
                }
            }
    );

    private final ActivityResultLauncher<String[]> pickPdf = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    List<Uri> uris = new ArrayList<>();
                    uris.add(uri);
                    showPreviewDialog(uris);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionFolderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CloudinaryHelper.init(this);

        folder = (PrescriptionFolderModel) getIntent().getSerializableExtra("folder");
        if (folder == null) {
            finish();
            return;
        }

        initView();
        loadPrescriptions();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.folderNameTxt.setText(folder.getFolderName());
        
        binding.prescriptionRv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new PrescriptionAdapter();
        binding.prescriptionRv.setAdapter(adapter);

        binding.uploadFab.setOnClickListener(v -> showUploadOptions());
    }

    private void showUploadOptions() {
        String[] options = {"Images", "PDF Document"};
        new AlertDialog.Builder(this)
                .setTitle("Select File Type")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickMedia.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else {
                        pickPdf.launch(new String[]{"application/pdf"});
                    }
                })
                .show();
    }

    private void showPreviewDialog(List<Uri> uris) {
        List<Uri> selectedUris = new ArrayList<>(uris);
        View view = getLayoutInflater().inflate(R.layout.dialog_image_preview, null);
        RecyclerView previewRv = view.findViewById(R.id.previewRv);
        
        ImagePreviewAdapter previewAdapter = new ImagePreviewAdapter(selectedUris, position -> {
            selectedUris.remove(position);
            // In a real app we'd update the adapter properly
        });
        previewRv.setAdapter(previewAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Upload Prescriptions")
                .setView(view)
                .setPositiveButton("Upload All", (dialog, which) -> {
                    for (Uri uri : selectedUris) {
                        uploadPrescription(uri);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isJpg(Uri uri) {
        String type = getContentResolver().getType(uri);
        return type != null && (type.equals("image/jpeg") || type.equals("image/jpg"));
    }

    private void loadPrescriptions() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference(Constants.NODE_PRESCRIPTIONS).child(uid).child(folder.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        prescriptionList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            PrescriptionModel model = data.getValue(PrescriptionModel.class);
                            if (model != null) {
                                model.setId(data.getKey());
                                prescriptionList.add(model);
                            }
                        }
                        Collections.sort(prescriptionList, (a, b) -> Long.compare(b.getUploadedAt(), a.getUploadedAt()));
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyTxt.setVisibility(prescriptionList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(PrescriptionFolderDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPrescription(Uri uri) {
        if (FirebaseHelper.getCurrentUser() == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseHelper.getUid();
        String folderId = (folder != null) ? folder.getId() : null;

        if (uid == null || folderId == null) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Internal error: Missing user or folder ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String prescriptionId = database.getReference(Constants.NODE_PRESCRIPTIONS).child(uid).child(folderId).push().getKey();
        if (prescriptionId == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        String mimeType = getContentResolver().getType(uri);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension == null) {
            if (mimeType != null && mimeType.contains("pdf")) extension = "pdf";
            else extension = "jpg";
        }

        String fileName = getFileName(uri);
        long fileSize = getFileSize(uri);
        String finalExtension = extension;

        CloudinaryHelper.uploadFile(uri, new CloudinaryHelper.UploadListener() {
            @Override
            public void onSuccess(String url) {
                saveMetadata(prescriptionId, uid, url, "", fileName, finalExtension, fileSize);
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(PrescriptionFolderDetailActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("PrescriptionUpload", "Error getting file name", e);
            }
        }
        if (result == null || result.isEmpty() || result.equals(".png") || result.equals(".jpg")) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return (result == null || result.isEmpty()) ? "unnamed_file" : result;
    }

    private long getFileSize(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.SIZE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void saveMetadata(String id, String uid, String url, String storagePath, String fileName, String type, long size) {
        PrescriptionModel model = new PrescriptionModel(id, uid, folder.getId(), url, storagePath, System.currentTimeMillis(), fileName, type, size);
        database.getReference(Constants.NODE_PRESCRIPTIONS).child(uid).child(folder.getId()).child(id).setValue(model)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Uploaded: " + fileName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Firebase Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deletePrescription(PrescriptionModel model) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Prescription")
                .setMessage("Are you sure you want to delete this prescription?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    database.getReference(Constants.NODE_PRESCRIPTIONS).child(model.getUserId()).child(model.getFolderId()).child(model.getId()).removeValue()
                            .addOnCompleteListener(task -> {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewholderPrescriptionBinding b = ViewholderPrescriptionBinding.inflate(getLayoutInflater(), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PrescriptionModel item = prescriptionList.get(position);
            
            if ("pdf".equals(item.getFileType())) {
                holder.binding.prescriptionImg.setImageResource(R.drawable.empty_box); // Placeholder for PDF
                // Use a proper PDF icon if available
            } else {
                Glide.with(PrescriptionFolderDetailActivity.this).load(item.getImageUrl()).into(holder.binding.prescriptionImg);
            }
            
            String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(item.getUploadedAt()));
            holder.binding.dateTxt.setText(dateStr);

            holder.itemView.setOnClickListener(v -> {
                if ("pdf".equals(item.getFileType())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(item.getImageUrl()), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(PrescriptionFolderDetailActivity.this, PrescriptionViewerActivity.class);
                    intent.putExtra("object", item);
                    startActivity(intent);
                }
            });

            holder.binding.deleteBtn.setOnClickListener(v -> deletePrescription(item));
            holder.binding.replaceBtn.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() { return prescriptionList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewholderPrescriptionBinding binding;
            ViewHolder(ViewholderPrescriptionBinding b) {
                super(b.getRoot());
                binding = b;
            }
        }
    }
}
