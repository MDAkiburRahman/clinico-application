package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.clinico.Domain.UserModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Helper.NotificationHelper;
import com.example.clinico.Utils.ValidationHelper;
import com.example.clinico.databinding.ActivityRegisterBinding;

public class RegisterActivity extends BaseActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerBtn.setOnClickListener(v -> {
            String name = ValidationHelper.sanitizeInput(binding.nameEt.getText().toString());
            String email = ValidationHelper.sanitizeInput(binding.emailEt.getText().toString());
            String password = binding.passwordEt.getText().toString();
            String confirmPassword = binding.confirmPasswordEt.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!ValidationHelper.isValidEmail(email)) {
                Toast.makeText(RegisterActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else if (!ValidationHelper.isStrongPassword(password)) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                binding.progressBar.setVisibility(View.VISIBLE);
                FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = FirebaseHelper.getUid();
                        if (uid != null) {
                            UserModel user = new UserModel(uid, name, email);
                            FirebaseHelper.getUsersRef().child(uid).setValue(user).addOnCompleteListener(task1 -> {
                                binding.progressBar.setVisibility(View.GONE);
                                if (task1.isSuccessful()) {
                                    NotificationHelper.updateFCMToken(RegisterActivity.this);
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Database error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.loginTxt.setOnClickListener(v -> finish());
    }
}
