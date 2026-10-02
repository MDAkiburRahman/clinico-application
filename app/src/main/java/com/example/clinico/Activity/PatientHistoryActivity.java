package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.DoctorAppointmentAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityPatientHistoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatientHistoryActivity extends BaseActivity {
    private ActivityPatientHistoryBinding binding;
    private List<AppointmentModel> historyList = new ArrayList<>();
    private DoctorAppointmentAdapter adapter;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        patientId = getIntent().getStringExtra("patientId");
        if (patientId == null) {
            finish();
            return;
        }

        initView();
        loadHistory();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.historyRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAppointmentAdapter(historyList, new DoctorAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onAccept(AppointmentModel appointment) {}
            @Override
            public void onReject(AppointmentModel appointment) {}
            @Override
            public void onDetails(AppointmentModel appointment) {}
            @Override
            public void onStartConsultation(AppointmentModel appointment) {}
        });
        binding.historyRv.setAdapter(adapter);
    }

    private void loadHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String doctorUid = FirebaseHelper.getUid();
        
        // Strategy: We need the Doctor's KEY first because Appointments store doctorId (Key), not UID
        FirebaseHelper.getDoctorsRef().orderByChild("uid").equalTo(doctorUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorId = snapshot.getChildren().iterator().next().getKey();
                    fetchAppointments(doctorId);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { binding.progressBar.setVisibility(View.GONE); }
        });
    }

    private void fetchAppointments(String doctorId) {
        FirebaseHelper.getAppointmentsRef().orderByChild("userId").equalTo(patientId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AppointmentModel app = ds.getValue(AppointmentModel.class);
                            if (app != null && doctorId.equals(app.getDoctorId())) {
                                historyList.add(app);
                            }
                        }
                        Collections.reverse(historyList);
                        adapter.notifyDataSetChanged();
                        binding.emptyTxt.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
