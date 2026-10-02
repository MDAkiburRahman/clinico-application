package com.example.clinico.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.PrescriptionFolderModel;
import com.example.clinico.Domain.PrescriptionModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityPrescriptionViewerBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionViewerActivity extends BaseActivity {
    private ActivityPrescriptionViewerBinding binding;
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private PrescriptionModel prescription;

    private final ActivityResultLauncher<String> pickNewImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    replaceImage(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prescription = (PrescriptionModel) getIntent().getSerializableExtra("object");
        if (prescription == null) {
            finish();
            return;
        }

        Glide.with(this).load(prescription.getImageUrl()).into(binding.fullImageView);

        initView();
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    private void initView() {
        binding.closeBtn.setOnClickListener(v -> finish());
        binding.deleteBtn.setOnClickListener(v -> confirmDelete());
        binding.replaceBtn.setOnClickListener(v -> pickNewImage.launch("image/*"));
        binding.moveBtn.setOnClickListener(v -> showMoveDialog());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Prescription")
                .setMessage("Are you sure you want to delete this prescription?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseHelper.getStorage().getReference().child(prescription.getStoragePath()).delete();
                    database.getReference(Constants.NODE_PRESCRIPTIONS).child(prescription.getUserId())
                            .child(prescription.getFolderId()).child(prescription.getId()).removeValue();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void replaceImage(Uri uri) {
        StorageReference storageRef = FirebaseHelper.getStorage().getReference().child(prescription.getStoragePath());
        
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] data = baos.toByteArray();

            storageRef.putBytes(data).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    prescription.setImageUrl(downloadUri.toString());
                    prescription.setUploadedAt(System.currentTimeMillis());
                    database.getReference(Constants.NODE_PRESCRIPTIONS).child(prescription.getUserId())
                            .child(prescription.getFolderId()).child(prescription.getId()).setValue(prescription);
                    
                    Glide.with(this).load(prescription.getImageUrl()).into(binding.fullImageView);
                    Toast.makeText(this, "Replaced successfully", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMoveDialog() {
        List<PrescriptionFolderModel> folders = new ArrayList<>();
        List<String> folderNames = new ArrayList<>();
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference(Constants.NODE_PRESCRIPTION_FOLDERS).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PrescriptionFolderModel f = ds.getValue(PrescriptionFolderModel.class);
                            if (f != null && !f.getId().equals(prescription.getFolderId())) {
                                folders.add(f);
                                folderNames.add(f.getFolderName());
                            }
                        }

                        if (folderNames.isEmpty()) {
                            Toast.makeText(PrescriptionViewerActivity.this, "No other folders found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new AlertDialog.Builder(PrescriptionViewerActivity.this)
                                .setTitle("Move to Folder")
                                .setItems(folderNames.toArray(new String[0]), (dialog, which) -> {
                                    movePrescription(folders.get(which));
                                })
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void movePrescription(PrescriptionFolderModel targetFolder) {
        String oldFolderId = prescription.getFolderId();
        String userId = prescription.getUserId();
        String id = prescription.getId();

        // 1. Remove from old path in metadata
        database.getReference(Constants.NODE_PRESCRIPTIONS).child(userId).child(oldFolderId).child(id).removeValue();
        
        // 2. Add to new path in metadata
        prescription.setFolderId(targetFolder.getId());
        database.getReference(Constants.NODE_PRESCRIPTIONS).child(userId).child(targetFolder.getId()).child(id).setValue(prescription)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Moved to " + targetFolder.getFolderName(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        
        // Note: For absolute consistency, the Storage path could also be changed, but since the metadata 
        // points to the URL and the DB hierarchy is updated, the app logic will stay correct.
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            binding.fullImageView.setScaleX(mScaleFactor);
            binding.fullImageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}
