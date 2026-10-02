package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.TopDoctorsAdapter;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.databinding.ActivityFavoritesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends BaseActivity {
    private ActivityFavoritesBinding binding;
    private List<DoctorsModel> favoriteDoctors = new ArrayList<>();
    private TopDoctorsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadFavorites();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.favoritesRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TopDoctorsAdapter(favoriteDoctors);
        binding.favoritesRv.setAdapter(adapter);
    }

    private void loadFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Favorites").child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        favoriteDoctors.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                DoctorsModel doctor = data.getValue(DoctorsModel.class);
                                if (doctor != null) {
                                    favoriteDoctors.add(doctor);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        
                        if (favoriteDoctors.isEmpty()) {
                            binding.noFavoritesTxt.setVisibility(View.VISIBLE);
                        } else {
                            binding.noFavoritesTxt.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
