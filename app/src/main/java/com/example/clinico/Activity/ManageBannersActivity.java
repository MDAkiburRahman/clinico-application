package com.example.clinico.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AdminBannerAdapter;
import com.example.clinico.Domain.BannerModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityManageBannersBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageBannersActivity extends BaseActivity {
    private ActivityManageBannersBinding binding;
    private Uri imageUri;
    private List<BannerModel> bannerList = new ArrayList<>();
    private AdminBannerAdapter adapter;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.previewImg.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageBannersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CloudinaryHelper.init(this);
        initView();
        loadBanners();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.selectBtn.setOnClickListener(v -> getContent.launch("image/*"));

        binding.uploadBtn.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadBanner();
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        binding.bannerRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBannerAdapter(bannerList, (banner, position) -> deleteBanner(banner));
        binding.bannerRv.setAdapter(adapter);
    }

    private void loadBanners() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Banners").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    BannerModel banner = data.getValue(BannerModel.class);
                    if (banner != null) {
                        banner.setKey(data.getKey());
                        bannerList.add(banner);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void uploadBanner() {
        binding.progressBar.setVisibility(View.VISIBLE);
        CloudinaryHelper.uploadFile(imageUri, new CloudinaryHelper.UploadListener() {
            @Override
            public void onSuccess(String url) {
                saveBannerToFirebase(url);
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageBannersActivity.this, "Upload Failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBannerToFirebase(String url) {
        String key = database.getReference("Banners").push().getKey();
        BannerModel banner = new BannerModel(url);
        if (key != null) {
            database.getReference("Banners").child(key).setValue(banner).addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(ManageBannersActivity.this, "Banner Added Successfully", Toast.LENGTH_SHORT).show();
                    imageUri = null;
                    binding.previewImg.setImageResource(R.drawable.empty_box);
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                    Toast.makeText(ManageBannersActivity.this, "Database Error: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void deleteBanner(BannerModel banner) {
        if (banner.getKey() == null) return;
        
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Banners").child(banner.getKey()).removeValue().addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Banner Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
