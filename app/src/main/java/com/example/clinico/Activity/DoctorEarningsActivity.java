package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.DoctorAppointmentAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityDoctorEarningsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorEarningsActivity extends BaseActivity {
    private ActivityDoctorEarningsBinding binding;
    private List<AppointmentModel> completedAppointments = new ArrayList<>();
    private DoctorAppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorEarningsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadEarnings();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.transactionsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAppointmentAdapter(completedAppointments, new DoctorAppointmentAdapter.OnAppointmentActionListener() {
            @Override public void onAccept(AppointmentModel appointment) {}
            @Override public void onReject(AppointmentModel appointment) {}
            @Override public void onDetails(AppointmentModel appointment) {}
            @Override public void onStartConsultation(AppointmentModel appointment) {}
        });
        binding.transactionsRv.setAdapter(adapter);
    }

    private void loadEarnings() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getDoctorsRef().orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorId = snapshot.getChildren().iterator().next().getKey();
                    fetchCompletedAppointments(doctorId);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { binding.progressBar.setVisibility(View.GONE); }
        });
    }

    private void fetchCompletedAppointments(String doctorId) {
        FirebaseHelper.getAppointmentsRef().orderByChild("doctorId").equalTo(doctorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        completedAppointments.clear();
                        double total = 0, today = 0, week = 0;
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        String todayStr = sdf.format(new Date());
                        
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                        long weekStart = cal.getTimeInMillis();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AppointmentModel app = ds.getValue(AppointmentModel.class);
                            if (app != null && "Completed".equals(app.getStatus())) {
                                completedAppointments.add(app);
                                double fee = 0;
                                try { fee = Double.parseDouble(app.getConsultationFee()); } catch (Exception ignored) {}
                                
                                total += fee;
                                if (todayStr.equals(app.getAppointmentDate())) today += fee;
                                if (app.getBookingTime() >= weekStart) week += fee;
                            }
                        }
                        
                        Collections.reverse(completedAppointments);
                        adapter.notifyDataSetChanged();
                        
                        binding.totalEarningsTxt.setText("৳ " + total);
                        binding.todayEarningsTxt.setText("৳ " + today);
                        binding.weekEarningsTxt.setText("৳ " + week);
                        
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
