package com.example.clinico.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityDoctorProfileBinding;

public class DoctorProfileActivity extends BaseActivity {
    private ActivityDoctorProfileBinding binding;
    private DoctorsModel doctor;
    private Uri imageUri;

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
        binding = ActivityDoctorProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        doctor = (DoctorsModel) getIntent().getSerializableExtra("doctor");
        if (doctor == null) {
            finish();
            return;
        }

        CloudinaryHelper.init(this);
        initView();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.doctorImg.setOnClickListener(v -> getContent.launch("image/*"));

        binding.nameEt.setText(doctor.getName());
        binding.specialEt.setText(doctor.getSpecial());
        binding.educationEt.setText(doctor.getEducation());
        binding.experienceEt.setText(String.valueOf(doctor.getExperience()));
        binding.locationEt.setText(doctor.getLocation());
        binding.priceEt.setText(String.valueOf(doctor.getPrice()));
        binding.languagesEt.setText(doctor.getLanguages());
        binding.biographyEt.setText(doctor.getBiography());

        Glide.with(this).load(doctor.getPicture()).placeholder(R.drawable.profile).into(binding.doctorImg);

        binding.saveBtn.setOnClickListener(v -> saveProfile());

        binding.reviewsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorReviewsActivity.class);
            intent.putExtra("doctorId", doctor.getKey());
            startActivity(intent);
        });

        binding.earningsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorEarningsActivity.class);
            startActivity(intent);
        });
    }

    private void saveProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveBtn.setEnabled(false);

        if (imageUri != null) {
            CloudinaryHelper.uploadFile(imageUri, new CloudinaryHelper.UploadListener() {
                @Override
                public void onSuccess(String url) {
                    doctor.setPicture(url);
                    updateFirebase();
                }

                @Override
                public void onError(String error) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveBtn.setEnabled(true);
                    Toast.makeText(DoctorProfileActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateFirebase();
        }
    }

    private void updateFirebase() {
        doctor.setName(binding.nameEt.getText().toString());
        doctor.setSpecial(binding.specialEt.getText().toString());
        doctor.setEducation(binding.educationEt.getText().toString());
        doctor.setExperience(Integer.parseInt(binding.experienceEt.getText().toString()));
        doctor.setLocation(binding.locationEt.getText().toString());
        doctor.setPrice(Integer.parseInt(binding.priceEt.getText().toString()));
        doctor.setLanguages(binding.languagesEt.getText().toString());
        doctor.setBiography(binding.biographyEt.getText().toString());

        FirebaseHelper.getDoctorsRef().child(doctor.getKey()).setValue(doctor)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveBtn.setEnabled(true);
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}
