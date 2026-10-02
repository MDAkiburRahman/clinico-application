package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AvailabilityDayAdapter;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityDoctorAvailabilityBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorAvailabilityActivity extends BaseActivity {
    private ActivityDoctorAvailabilityBinding binding;
    private String doctorKey;
    private Map<String, List<DoctorsModel.TimeSlot>> availability = new HashMap<>();
    private List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private AvailabilityDayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorAvailabilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadAvailability();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.daysRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AvailabilityDayAdapter(days, availability, this::saveAvailability);
        binding.daysRv.setAdapter(adapter);
    }

    private void loadAvailability() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getDoctorsRef().orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot ds = snapshot.getChildren().iterator().next();
                    doctorKey = ds.getKey();
                    DoctorsModel doctor = ds.getValue(DoctorsModel.class);
                    if (doctor != null && doctor.getAvailability() != null) {
                        availability.putAll(doctor.getAvailability());
                        adapter.notifyDataSetChanged();
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { binding.progressBar.setVisibility(View.GONE); }
        });
    }

    private void saveAvailability() {
        if (doctorKey == null) return;
        // The adapter modifies the 'availability' map directly.
        FirebaseHelper.getDoctorsRef().child(doctorKey).child("availability").setValue(availability)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Availability updated", Toast.LENGTH_SHORT).show());
    }
}
