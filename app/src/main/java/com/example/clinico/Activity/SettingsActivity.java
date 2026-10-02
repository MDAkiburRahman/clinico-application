package com.example.clinico.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.clinico.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity {
    private ActivitySettingsBinding binding;
    private SharedPreferences settingsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsPrefs = getSharedPreferences("Settings", MODE_PRIVATE);

        initView();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        // Dark Mode Logic
        binding.darkModeSwitch.setChecked(sessionManager.isDarkMode());
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setDarkMode(isChecked);
            applyTheme();
        });

        // Notifications Logic
        boolean notificationsEnabled = settingsPrefs.getBoolean("notifications", true);
        binding.notificationSwitch.setChecked(notificationsEnabled);
        binding.notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("notifications", isChecked).apply();
            Toast.makeText(this, "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        binding.privacyPolicyTxt.setOnClickListener(v -> openUrl("https://www.example.com/privacy"));
        binding.termsTxt.setOnClickListener(v -> openUrl("https://www.example.com/terms"));
        binding.aboutTxt.setOnClickListener(v -> {
            Toast.makeText(this, "Clinico v1.0\nDeveloped by Expert Dev", Toast.LENGTH_LONG).show();
        });

        binding.deleteAccountTxt.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteAccount() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
