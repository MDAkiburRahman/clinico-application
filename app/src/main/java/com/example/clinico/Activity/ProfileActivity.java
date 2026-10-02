package com.example.clinico.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.UserModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.databinding.ActivityProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ProfileActivity extends BaseActivity {
    private ActivityProfileBinding binding;
    private Uri imageUri;
    private UserModel userModel;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.profileImg.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CloudinaryHelper.init(this);

        initView();
        loadUserData();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.editImgBtn.setOnClickListener(v -> getContent.launch("image/*"));

        binding.dobEt.setOnClickListener(v -> showDatePicker());

        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.genderSpinner.setAdapter(adapter);

        binding.saveBtn.setOnClickListener(v -> saveProfile());

        binding.changePasswordTxt.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
        });

        binding.logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.myOrdersBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OrderHistoryActivity.class));
        });

        binding.adminBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class));
        });
    }

    private void loadUserData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Users").child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            userModel = snapshot.getValue(UserModel.class);
                            if (userModel != null) {
                                if (userModel.isAdmin()) {
                                    binding.adminBtn.setVisibility(View.VISIBLE);
                                }
                                binding.nameEt.setText(userModel.getName());
                                binding.emailEt.setText(userModel.getEmail());
                                binding.phoneEt.setText(userModel.getPhone());
                                binding.dobEt.setText(userModel.getDob());
                                binding.bloodGroupEt.setText(userModel.getBloodGroup());
                                binding.addressEt.setText(userModel.getAddress());

                                if (userModel.getGender() != null) {
                                    int pos = 0;
                                    if (userModel.getGender().equals("Female")) pos = 1;
                                    else if (userModel.getGender().equals("Other")) pos = 2;
                                    binding.genderSpinner.setSelection(pos);
                                }

                                if (userModel.getProfilePic() != null && !userModel.getProfilePic().isEmpty()) {
                                    Glide.with(ProfileActivity.this)
                                            .load(userModel.getProfilePic())
                                            .into(binding.profileImg);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String dob = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            binding.dobEt.setText(dob);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveProfile() {
        String name = binding.nameEt.getText().toString();
        String phone = binding.phoneEt.getText().toString();
        String dob = binding.dobEt.getText().toString();
        String bloodGroup = binding.bloodGroupEt.getText().toString();
        String address = binding.addressEt.getText().toString();
        String gender = binding.genderSpinner.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            CloudinaryHelper.uploadFile(imageUri, new CloudinaryHelper.UploadListener() {
                @Override
                public void onSuccess(String url) {
                    updateFirebase(name, phone, dob, bloodGroup, address, gender, url);
                }

                @Override
                public void onError(String error) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Image Upload Failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String existingImg = (userModel != null) ? userModel.getProfilePic() : null;
            updateFirebase(name, phone, dob, bloodGroup, address, gender, existingImg);
        }
    }

    private void updateFirebase(String name, String phone, String dob, String bloodGroup, String address, String gender, String imageUrl) {
        String uid = mAuth.getCurrentUser().getUid();
        if (userModel == null) {
            userModel = new UserModel(uid, name, mAuth.getCurrentUser().getEmail());
        }
        userModel.setName(name);
        userModel.setPhone(phone);
        userModel.setDob(dob);
        userModel.setBloodGroup(bloodGroup);
        userModel.setAddress(address);
        userModel.setGender(gender);
        userModel.setProfilePic(imageUrl);

        database.getReference("Users").child(uid).setValue(userModel).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
