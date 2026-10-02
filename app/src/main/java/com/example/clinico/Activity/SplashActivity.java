package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends BaseActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivitySplashBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            loadAppLogo(binding.imageView);

            binding.startBtn.setOnClickListener(v -> {
                try {
                    if (mAuth != null && mAuth.getCurrentUser() != null) {
                        String role = sessionManager.getUserRole();
                        Intent intent;
                        if (Constants.ROLE_DOCTOR.equals(role)) {
                            intent = new Intent(SplashActivity.this, DoctorMainActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        }
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    android.widget.Toast.makeText(this, "Transition error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
