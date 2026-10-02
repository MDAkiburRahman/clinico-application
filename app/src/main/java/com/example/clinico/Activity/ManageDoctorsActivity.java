package com.example.clinico.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AdminDoctorAdapter;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.databinding.ActivityManageDoctorsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageDoctorsActivity extends BaseActivity {
    private ActivityManageDoctorsBinding binding;
    private List<DoctorsModel> doctorList = new ArrayList<>();
    private AdminDoctorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageDoctorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadDoctors();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.addDoctorFab.setOnClickListener(v -> {
            startActivity(new Intent(ManageDoctorsActivity.this, AddDoctorActivity.class));
        });

        binding.doctorsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminDoctorAdapter(doctorList, new AdminDoctorAdapter.OnDoctorActionListener() {
            @Override
            public void onEdit(DoctorsModel doctor) {
                Intent intent = new Intent(ManageDoctorsActivity.this, AddDoctorActivity.class);
                intent.putExtra("doctor", doctor);
                startActivity(intent);
            }

            @Override
            public void onDelete(DoctorsModel doctor) {
                confirmDelete(doctor);
            }

            @Override
            public void onFeaturedToggle(DoctorsModel doctor, boolean isFeatured) {
                updateFeaturedStatus(doctor, isFeatured);
            }

            @Override
            public void onManageAvailability(DoctorsModel doctor) {
                Intent intent = new Intent(ManageDoctorsActivity.this, DoctorAvailabilityActivity.class);
                intent.putExtra("doctor", doctor);
                startActivity(intent);
            }
        });
        binding.doctorsRv.setAdapter(adapter);
    }

    private void loadDoctors() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Doctors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    DoctorsModel doctor = data.getValue(DoctorsModel.class);
                    if (doctor != null) {
                        doctor.setKey(data.getKey());
                        doctorList.add(doctor);
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

    private void updateFeaturedStatus(DoctorsModel doctor, boolean isFeatured) {
        if (doctor.getKey() == null) return;
        database.getReference("Doctors").child(doctor.getKey()).child("featured").setValue(isFeatured);
    }

    private void confirmDelete(DoctorsModel doctor) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Doctor")
                .setMessage("Are you sure you want to remove " + doctor.getName() + " from the database?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    deleteDoctor(doctor);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDoctor(DoctorsModel doctor) {
        if (doctor.getKey() == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Doctors").child(doctor.getKey()).removeValue().addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(ManageDoctorsActivity.this, "Doctor removed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
