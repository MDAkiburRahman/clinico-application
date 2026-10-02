package com.example.clinico.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import com.example.clinico.Domain.ConsultantModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.R;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityAddConsultantBinding;

public class AddConsultantActivity extends BaseActivity {
    private ActivityAddConsultantBinding binding;
    private Uri imageUri;
    private ConsultantModel editConsultant;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.consultantImg.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddConsultantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editConsultant = (ConsultantModel) getIntent().getSerializableExtra("consultant");
        if (editConsultant != null) {
            isEditMode = true;
            setupEditData();
        }

        CloudinaryHelper.init(this);
        initView();
    }

    private void setupEditData() {
        binding.titleTxt.setText("Edit Consultant");
        binding.nameEt.setText(editConsultant.getName());
        binding.specialEt.setText(editConsultant.getSpecial());
        binding.phoneEt.setText(editConsultant.getPhone());
        binding.expEt.setText(String.valueOf(editConsultant.getExperience()));
        binding.bioEt.setText(editConsultant.getBio());
        
        Glide.with(this).load(editConsultant.getPicture()).placeholder(R.drawable.ic_person).into(editConsultant.getPicture() != null ? binding.consultantImg : null);
        if (editConsultant.getPicture() != null) {
             Glide.with(this).load(editConsultant.getPicture()).into(binding.consultantImg);
        }

        binding.saveBtn.setText("Update Consultant");
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.selectImgBtn.setOnClickListener(v -> getContent.launch("image/*"));
        binding.saveBtn.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        String name = binding.nameEt.getText().toString();
        String special = binding.specialEt.getText().toString();
        String phone = binding.phoneEt.getText().toString();
        String exp = binding.expEt.getText().toString();
        String bio = binding.bioEt.getText().toString();

        if (name.isEmpty() || special.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            CloudinaryHelper.uploadFile(imageUri, new CloudinaryHelper.UploadListener() {
                @Override
                public void onSuccess(String url) {
                    saveToFirebase(name, special, phone, exp, bio, url);
                }

                @Override
                public void onError(String error) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddConsultantActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveToFirebase(name, special, phone, exp, bio, isEditMode ? editConsultant.getPicture() : "");
        }
    }

    private void saveToFirebase(String name, String special, String phone, String exp, String bio, String imgUrl) {
        String id = isEditMode ? editConsultant.getId() : database.getReference(Constants.NODE_CONSULTANTS).push().getKey();
        if (id == null) return;

        ConsultantModel consultant = new ConsultantModel(
                id, name, special, phone, imgUrl, isEditMode ? editConsultant.isOnline() : true, bio, 
                exp.isEmpty() ? 0 : Integer.parseInt(exp)
        );

        database.getReference(Constants.NODE_CONSULTANTS).child(id).setValue(consultant).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Consultant saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Database error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
