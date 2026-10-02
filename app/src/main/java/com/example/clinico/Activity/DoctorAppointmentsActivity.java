package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.DoctorAppointmentAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityDoctorAppointmentsBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorAppointmentsActivity extends BaseActivity {
    private ActivityDoctorAppointmentsBinding binding;
    private List<AppointmentModel> allAppointments = new ArrayList<>();
    private List<AppointmentModel> filteredAppointments = new ArrayList<>();
    private DoctorAppointmentAdapter adapter;
    private String selectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String filter = getIntent().getStringExtra("filter");
        if (filter != null) {
            selectedStatus = filter;
        }

        initView();
        loadAppointments();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.appointmentsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAppointmentAdapter(filteredAppointments, new DoctorAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onAccept(AppointmentModel appointment) { updateStatus(appointment, "Confirmed"); }
            @Override
            public void onReject(AppointmentModel appointment) { showRejectionDialog(appointment); }
            @Override
            public void onDetails(AppointmentModel appointment) {
                Intent intent = new Intent(DoctorAppointmentsActivity.this, DoctorAppointmentDetailActivity.class);
                intent.putExtra("appointment", appointment);
                startActivity(intent);
            }
            @Override
            public void onStartConsultation(AppointmentModel appointment) {
                Intent intent = new Intent(DoctorAppointmentsActivity.this, ConsultationActivity.class);
                intent.putExtra("appointment", appointment);
                startActivity(intent);
            }
        });
        binding.appointmentsRv.setAdapter(adapter);

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                selectedStatus = chip.getText().toString();
                filter();
            }
        });

        // Set initial chip selection
        if (!selectedStatus.equals("All")) {
            for (int i = 0; i < binding.filterChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) binding.filterChipGroup.getChildAt(i);
                if (chip.getText().toString().equals(selectedStatus)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void loadAppointments() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getDoctorsRef().orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorId = snapshot.getChildren().iterator().next().getKey();
                    FirebaseHelper.getAppointmentsRef().orderByChild("doctorId").equalTo(doctorId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    allAppointments.clear();
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        AppointmentModel app = ds.getValue(AppointmentModel.class);
                                        if (app != null) allAppointments.add(app);
                                    }
                                    Collections.reverse(allAppointments);
                                    filter();
                                    binding.progressBar.setVisibility(View.GONE);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { binding.progressBar.setVisibility(View.GONE); }
                            });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { binding.progressBar.setVisibility(View.GONE); }
        });
    }

    private void filter() {
        String query = binding.searchEt.getText().toString().toLowerCase().trim();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

        filteredAppointments.clear();
        for (AppointmentModel app : allAppointments) {
            boolean matchesStatus = selectedStatus.equals("All") || 
                    (selectedStatus.equals("Today") && today.equals(app.getAppointmentDate())) ||
                    app.getStatus().equalsIgnoreCase(selectedStatus);
            
            boolean matchesQuery = query.isEmpty() || 
                    app.getAppointmentId().toLowerCase().contains(query) ||
                    (app.getUserName() != null && app.getUserName().toLowerCase().contains(query));

            if (matchesStatus && matchesQuery) {
                filteredAppointments.add(app);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateStatus(AppointmentModel app, String status) {
        FirebaseHelper.getAppointmentsRef().child(app.getAppointmentId()).child("status").setValue(status);
    }

    private void showRejectionDialog(AppointmentModel app) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Reason for rejection");
        new android.app.AlertDialog.Builder(this)
                .setTitle("Reject Appointment")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = input.getText().toString();
                    FirebaseHelper.getAppointmentsRef().child(app.getAppointmentId()).child("rejectionNote").setValue(reason);
                    updateStatus(app, "Rejected");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
