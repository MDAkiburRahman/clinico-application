package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinico.Domain.PrescriptionFolderModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityPrescriptionBinding;
import com.example.clinico.databinding.ViewholderPrescriptionFolderBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrescriptionActivity extends BaseActivity {
    private ActivityPrescriptionBinding binding;
    private List<PrescriptionFolderModel> folderList = new ArrayList<>();
    private FolderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadFolders();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.folderRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FolderAdapter();
        binding.folderRv.setAdapter(adapter);

        binding.createFolderFab.setOnClickListener(v -> showCreateFolderDialog(null));
    }

    private void loadFolders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseHelper.getUid();
        if (uid == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        database.getReference(Constants.NODE_PRESCRIPTION_FOLDERS).child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        folderList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            PrescriptionFolderModel model = data.getValue(PrescriptionFolderModel.class);
                            if (model != null) {
                                model.setId(data.getKey());
                                folderList.add(model);
                            }
                        }
                        Collections.sort(folderList, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyTxt.setVisibility(folderList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(PrescriptionActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateFolderDialog(PrescriptionFolderModel editModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(editModel == null ? "Create Folder" : "Rename Folder");

        EditText input = new EditText(this);
        input.setHint("e.g. Gynecology, Dentist, etc.");
        if (editModel != null) input.setText(editModel.getFolderName());
        
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        builder.setView(input);
        // Add padding to the view manually if needed, or just use a layout file.

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                saveFolder(editModel, name);
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveFolder(PrescriptionFolderModel model, String name) {
        if (FirebaseHelper.getCurrentUser() == null) {
            Toast.makeText(this, "Your session has expired. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseHelper.getUid();
        
        String folderId = (model == null) ? database.getReference(Constants.NODE_PRESCRIPTION_FOLDERS).child(uid).push().getKey() : model.getId();
        if (folderId == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        PrescriptionFolderModel newModel = (model == null) ? 
                new PrescriptionFolderModel(folderId, uid, name, System.currentTimeMillis()) : model;
        newModel.setFolderName(name);

        String path = Constants.NODE_PRESCRIPTION_FOLDERS + "/" + uid + "/" + folderId;
        database.getReference(Constants.NODE_PRESCRIPTION_FOLDERS).child(uid).child(folderId).setValue(newModel)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Folder saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                        android.util.Log.e("PrescriptionActivity", "Folder creation failed. Path: " + path + ", Error: " + err);
                        Toast.makeText(this, "Firebase Error: " + err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteFolder(PrescriptionFolderModel model) {
        if (FirebaseHelper.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Folder")
                .setMessage("This folder contains prescriptions. Are you sure you want to delete it and all its contents?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    String uid = FirebaseHelper.getUid();

                    // Delete prescriptions metadata hierarchy
                    database.getReference(Constants.NODE_PRESCRIPTIONS).child(uid).child(model.getId()).removeValue();

                    // Delete folder metadata
                    database.getReference(Constants.NODE_PRESCRIPTION_FOLDERS).child(uid).child(model.getId()).removeValue()
                            .addOnCompleteListener(task -> {
                                binding.progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(PrescriptionActivity.this, "Folder removed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PrescriptionActivity.this, "Failed to remove folder", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewholderPrescriptionFolderBinding b = ViewholderPrescriptionFolderBinding.inflate(getLayoutInflater(), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PrescriptionFolderModel item = folderList.get(position);
            holder.binding.folderNameTxt.setText(item.getFolderName());

            // Count items in this folder (Hierarchical path)
            String uid = FirebaseHelper.getUid();
            if (uid != null) {
                database.getReference(Constants.NODE_PRESCRIPTIONS).child(uid).child(item.getId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                long count = snapshot.getChildrenCount();
                                holder.binding.itemCountTxt.setText(count + " items");
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PrescriptionActivity.this, PrescriptionFolderDetailActivity.class);
                intent.putExtra("folder", item);
                startActivity(intent);
            });

            holder.binding.optionsBtn.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(PrescriptionActivity.this, v);
                popup.getMenu().add("Rename");
                popup.getMenu().add("Delete");
                popup.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getTitle().equals("Rename")) {
                        showCreateFolderDialog(item);
                    } else {
                        deleteFolder(item);
                    }
                    return true;
                });
                popup.show();
            });
        }

        @Override
        public int getItemCount() { return folderList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewholderPrescriptionFolderBinding binding;
            ViewHolder(ViewholderPrescriptionFolderBinding b) {
                super(b.getRoot());
                binding = b;
            }
        }
    }
}
