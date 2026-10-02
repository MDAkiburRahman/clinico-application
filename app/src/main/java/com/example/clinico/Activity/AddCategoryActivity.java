package com.example.clinico.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.databinding.ActivityAddCategoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AddCategoryActivity extends BaseActivity {
    private ActivityAddCategoryBinding binding;
    private Uri imageUri;
    private CategoryModel editCategory;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.catImg.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editCategory = (CategoryModel) getIntent().getSerializableExtra("category");
        if (editCategory != null) {
            isEditMode = true;
        }

        CloudinaryHelper.init(this);
        initView();

        if (isEditMode) {
            setupEditData();
        }
    }

    private void setupEditData() {
        binding.nameEt.setText(editCategory.getName());
        com.bumptech.glide.Glide.with(this)
                .load(editCategory.getPicture())
                .into(binding.catImg);
        binding.addCatBtn.setText("Update Category");
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.selectImgBtn.setOnClickListener(v -> getContent.launch("image/*"));
        binding.addCatBtn.setOnClickListener(v -> {
            String name = binding.nameEt.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isEditMode && imageUri == null) {
                Toast.makeText(this, "Image required", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadAndSave(name);
        });
    }

    private void uploadAndSave(String name) {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (imageUri != null) {
            CloudinaryHelper.uploadFile(imageUri, new CloudinaryHelper.UploadListener() {
                @Override
                public void onSuccess(String url) {
                    saveToFirebase(name, url);
                }

                @Override
                public void onError(String error) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddCategoryActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveToFirebase(name, editCategory.getPicture());
        }
    }

    private void saveToFirebase(String name, String url) {
        CategoryModel category = isEditMode ? editCategory : new CategoryModel();
        category.setName(name);
        category.setPicture(url);

        if (isEditMode && editCategory.getKey() != null) {
            database.getReference("Category").child(editCategory.getKey()).setValue(category)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(AddCategoryActivity.this, "Category updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            database.getReference("Category").push().setValue(category).addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(AddCategoryActivity.this, "Category Added", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}
