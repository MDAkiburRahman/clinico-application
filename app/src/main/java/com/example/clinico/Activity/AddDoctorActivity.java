package com.example.clinico.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityAddDoctorBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddDoctorActivity extends BaseActivity {
    private ActivityAddDoctorBinding binding;
    private Uri imageUri;
    private List<String> categories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private DoctorsModel editDoctor;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.doctorImg.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editDoctor = (DoctorsModel) getIntent().getSerializableExtra("doctor");
        if (editDoctor != null) {
            isEditMode = true;
        }

        CloudinaryHelper.init(this);
        initView();
        loadCategories();

        if (isEditMode) {
            setupEditData();
        }
    }

    private void setupEditData() {
        binding.nameEt.setText(editDoctor.getName());
        binding.emailEt.setText(editDoctor.getEmail());
        binding.addressEt.setText(editDoctor.getAddress());
        binding.experienceEt.setText(String.valueOf(editDoctor.getExperience()));
        binding.priceEt.setText(editDoctor.getPrice());
        binding.mobileEt.setText(editDoctor.getMobile());
        binding.siteEt.setText(editDoctor.getSite());
        binding.bioEt.setText(editDoctor.getBiography());
        
        Glide.with(this)
                .load(editDoctor.getPicture())
                .placeholder(R.drawable.ic_person)
                .into(binding.doctorImg);
        
        binding.addDoctorBtn.setText("Update Doctor Info");
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.selectImgBtn.setOnClickListener(v -> getContent.launch("image/*"));
        binding.addDoctorBtn.setOnClickListener(v -> validateAndUpload());

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.specialSpinner.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        database.getReference("Category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    CategoryModel cat = data.getValue(CategoryModel.class);
                    if (cat != null) {
                        categories.add(cat.getName());
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                if (categories.isEmpty()) {
                    categories.add("No categories found");
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void validateAndUpload() {
        String name = binding.nameEt.getText().toString();
        String email = binding.emailEt.getText().toString();
        String special = binding.specialSpinner.getSelectedItem() != null ? binding.specialSpinner.getSelectedItem().toString() : "";
        String address = binding.addressEt.getText().toString();
        String exp = binding.experienceEt.getText().toString();
        String price = binding.priceEt.getText().toString();
        String mobile = binding.mobileEt.getText().toString();
        String site = binding.siteEt.getText().toString();
        String bio = binding.bioEt.getText().toString();

        if (name.isEmpty() || email.isEmpty() || special.equals("No categories found")) {
            Toast.makeText(this, "Name, Email and Specialization are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEditMode && imageUri == null) {
            Toast.makeText(this, "Doctor Image is required", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        if (imageUri != null) {
            CloudinaryHelper.uploadFile(imageUri, new CloudinaryHelper.UploadListener() {
                @Override
                public void onSuccess(String url) {
                    saveToFirebase(name, email, special, address, exp, price, mobile, site, bio, url);
                }

                @Override
                public void onError(String error) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddDoctorActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveToFirebase(name, email, special, address, exp, price, mobile, site, bio, editDoctor.getPicture());
        }
    }

    private void saveToFirebase(String name, String email, String special, String address, String exp, String price, String mobile, String site, String bio, String imgUrl) {
        DoctorsModel doctor = isEditMode ? editDoctor : new DoctorsModel();
        if (!isEditMode) {
            doctor.setId((int) (System.currentTimeMillis() / 1000));
            doctor.setRating(5.0);
            doctor.setPatiens("0");
        }
        
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setSpecial(special);
        doctor.setAddress(address);
        doctor.setExperience(exp.isEmpty() ? 0 : Integer.parseInt(exp));
        doctor.setPrice(price);
        doctor.setMobile(mobile);
        doctor.setSite(site);
        doctor.setBiography(bio);
        doctor.setPicture(imgUrl);

        if (isEditMode && editDoctor.getKey() != null) {
            database.getReference("Doctors").child(editDoctor.getKey()).setValue(doctor)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(AddDoctorActivity.this, "Doctor updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            String id = database.getReference("Doctors").push().getKey();
            if (id != null) {
                database.getReference("Doctors").child(id).setValue(doctor).addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Doctor added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                        Toast.makeText(this, "Database error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
