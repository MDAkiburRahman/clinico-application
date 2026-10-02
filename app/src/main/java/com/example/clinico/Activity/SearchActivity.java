package com.example.clinico.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.PremiumDoctorAdapter;
import com.example.clinico.Adapter.SpecialtyAdapter;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.databinding.ActivitySearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends BaseActivity {
    private ActivitySearchBinding binding;
    private List<DoctorsModel> allDoctors = new ArrayList<>();
    private List<DoctorsModel> filteredList = new ArrayList<>();
    private PremiumDoctorAdapter doctorAdapter;
    private SpecialtyAdapter specialtyAdapter;
    private String currentSpecialty = "All";
    private String mode = "find";
    private String filter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "find";
        
        filter = getIntent().getStringExtra("filter");
        if (filter == null) filter = "all";

        initView();
        initSpecialties();
        loadAllDoctors();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        if ("book".equals(mode)) {
            binding.searchTitle.setText("Quick Book");
            binding.searchEt.setHint("Choose a doctor to book");
        } else if ("featured".equals(filter)) {
            binding.searchTitle.setText("Featured Doctors");
            binding.searchEt.setHint("Search featured doctors...");
        } else if ("top".equals(filter)) {
            binding.searchTitle.setText("All Doctors");
            binding.searchEt.setHint("Search all doctors...");
        }

        binding.searchRv.setLayoutManager(new LinearLayoutManager(this));
        doctorAdapter = new PremiumDoctorAdapter(filteredList);
        binding.searchRv.setAdapter(doctorAdapter);

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initSpecialties() {
        // Initially empty, will be populated from Firebase doctors
        binding.specialtyRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadAllDoctors() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Doctors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allDoctors.clear();
                List<String> specialties = new ArrayList<>();
                specialties.add("All");
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    DoctorsModel doctor = data.getValue(DoctorsModel.class);
                    if (doctor != null) {
                        doctor.setKey(data.getKey());
                        allDoctors.add(doctor);
                        
                        String spec = doctor.getSpecial();
                        if (spec != null && !specialties.contains(spec)) {
                            specialties.add(spec);
                        }
                    }
                }

                specialtyAdapter = new SpecialtyAdapter(specialties, specialty -> {
                    currentSpecialty = specialty;
                    applyFilters(binding.searchEt.getText().toString());
                });
                binding.specialtyRv.setAdapter(specialtyAdapter);
                
                String category = getIntent().getStringExtra("category");
                if (category != null && !category.isEmpty()) {
                    binding.searchEt.setText(category);
                    currentSpecialty = category;
                }
                
                applyFilters(binding.searchEt.getText().toString());
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void applyFilters(String query) {
        filteredList.clear();
        String q = query.toLowerCase().trim();

        for (DoctorsModel doctor : allDoctors) {
            if (doctor.isDisabled()) continue;

            boolean matchesQuery = q.isEmpty() || 
                    doctor.getName().toLowerCase().contains(q) || 
                    doctor.getSpecial().toLowerCase().contains(q) ||
                    (doctor.getEducation() != null && doctor.getEducation().toLowerCase().contains(q));
            
            boolean matchesSpecialty = currentSpecialty.equals("All") || 
                    doctor.getSpecial().equalsIgnoreCase(currentSpecialty);
            
            boolean matchesFilter = true;
            if ("featured".equals(filter)) {
                matchesFilter = doctor.isFeatured();
            }

            if (matchesQuery && matchesSpecialty && matchesFilter) {
                filteredList.add(doctor);
            }
        }

        doctorAdapter.notifyDataSetChanged();
        binding.noResultTxt.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
