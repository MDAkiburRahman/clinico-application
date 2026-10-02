package com.example.clinico.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityGlobalConfigBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GlobalConfigActivity extends BaseActivity {
    private ActivityGlobalConfigBinding binding;
    private Uri logoUri;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    logoUri = uri;
                    binding.logoPreview.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGlobalConfigBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CloudinaryHelper.init(this);
        initView();
        loadConfig();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.selectLogoBtn.setOnClickListener(v -> getContent.launch("image/*"));
        binding.saveBtn.setOnClickListener(v -> saveConfig());
    }

    private void loadConfig() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference(Constants.NODE_APP_CONFIG).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    String number = snapshot.child("ambulanceNumber").getValue(String.class);
                    String logoUrl = snapshot.child("logoUrl").getValue(String.class);

                    if (number != null) binding.emergencyNumberEt.setText(number);
                    if (logoUrl != null && !logoUrl.isEmpty()) {
                        Glide.with(GlobalConfigActivity.this).load(logoUrl).into(binding.logoPreview);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveConfig() {
        String number = binding.emergencyNumberEt.getText().toString();
        binding.progressBar.setVisibility(View.VISIBLE);

        if (logoUri != null) {
            CloudinaryHelper.uploadFile(logoUri, new CloudinaryHelper.UploadListener() {
                @Override
                public void onSuccess(String url) {
                    updateFirebase(number, url);
                }

                @Override
                public void onError(String error) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(GlobalConfigActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateFirebase(number, null);
        }
    }

    private void updateFirebase(String number, String logoUrl) {
        Map<String, Object> config = new HashMap<>();
        config.put("ambulanceNumber", number);
        if (logoUrl != null) config.put("logoUrl", logoUrl);

        database.getReference(Constants.NODE_APP_CONFIG).updateChildren(config).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Configuration Saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Failed to save: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
