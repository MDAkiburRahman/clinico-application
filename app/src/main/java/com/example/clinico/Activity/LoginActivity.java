package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Helper.NotificationHelper;
import com.example.clinico.R;
import com.example.clinico.Utils.Constants;
import com.example.clinico.Utils.ValidationHelper;
import com.example.clinico.databinding.ActivityLoginBinding;
import com.google.firebase.database.DataSnapshot;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            if (sessionManager == null) {
                sessionManager = new com.example.clinico.Utils.SessionManager(this);
            }

            String savedEmail = sessionManager.getUserEmail();
            if (savedEmail != null && !savedEmail.isEmpty()) {
                binding.emailEt.setText(savedEmail);
                binding.rememberMeCb.setChecked(true);
            }

            initView();
            loadAppLogo(binding.imageView2);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Login initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initView() {
        binding.loginBtn.setOnClickListener(v -> {
            String email = ValidationHelper.sanitizeInput(binding.emailEt.getText().toString());
            String password = binding.passwordEt.getText().toString();
            
            int checkedId = binding.roleToggleGroup.getCheckedButtonId();
            String selectedRole = (checkedId == R.id.doctorBtn) ? Constants.ROLE_DOCTOR : Constants.ROLE_PATIENT;

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!ValidationHelper.isValidEmail(email)) {
                Toast.makeText(LoginActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            } else {
                binding.progressBar.setVisibility(View.VISIBLE);
                try {
                    FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            verifyRole(selectedRole, email);
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Auth error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.registerTxt.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        binding.forgotPasswordTxt.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void verifyRole(String selectedRole, String email) {
        String uid = FirebaseHelper.getUid();
        
        if (selectedRole.equals(Constants.ROLE_PATIENT)) {
            FirebaseHelper.getUsersRef().child(uid).get().addOnCompleteListener(task -> 
                handleRoleResult(task.isSuccessful() && task.getResult().exists(), selectedRole, email)
            );
        } else {
            // For Doctors, search by email to support admin-added doctors who haven't logged in yet
            FirebaseHelper.getDoctorsRef().orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
                boolean exists = task.isSuccessful() && task.getResult().exists();
                if (exists) {
                    // Link UID if it's the first login
                    DataSnapshot doctorSnap = task.getResult().getChildren().iterator().next();
                    if (!doctorSnap.hasChild("uid")) {
                        doctorSnap.getRef().child("uid").setValue(uid);
                    }
                }
                handleRoleResult(exists, selectedRole, email);
            });
        }
    }

    private void handleRoleResult(boolean success, String selectedRole, String email) {
        binding.progressBar.setVisibility(View.GONE);
        if (success) {
            if (binding.rememberMeCb.isChecked()) {
                sessionManager.saveUserEmail(email);
            } else {
                sessionManager.saveUserEmail("");
            }
            sessionManager.saveUserRole(selectedRole);
            NotificationHelper.updateFCMToken(LoginActivity.this);

            Intent intent;
            if (selectedRole.equals(Constants.ROLE_DOCTOR)) {
                intent = new Intent(LoginActivity.this, DoctorMainActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            FirebaseHelper.getAuth().signOut();
            Toast.makeText(LoginActivity.this, "This account is not registered as a " + selectedRole, Toast.LENGTH_LONG).show();
        }
    }
}
