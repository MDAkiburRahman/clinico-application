package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.clinico.Adapter.DoctorAppointmentAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityDoctorMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorMainActivity extends BaseActivity {
    private ActivityDoctorMainBinding binding;
    private DoctorsModel currentDoctor;
    private List<AppointmentModel> todayList = new ArrayList<>();
    private List<AppointmentModel> upcomingList = new ArrayList<>();
    private DoctorAppointmentAdapter todayAdapter;
    private DoctorAppointmentAdapter upcomingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadDoctorData();
    }

    private void initView() {
        binding.currentDateTxt.setText(new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(new Date()));

        binding.todayRv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        todayAdapter = new DoctorAppointmentAdapter(todayList, new DoctorAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onAccept(AppointmentModel appointment) { updateAppointmentStatus(appointment, "Confirmed"); }
            @Override
            public void onReject(AppointmentModel appointment) { showRejectionDialog(appointment); }
            @Override
            public void onDetails(AppointmentModel appointment) { navigateToDetails(appointment); }
            @Override
            public void onStartConsultation(AppointmentModel appointment) { startConsultation(appointment); }
        });
        binding.todayRv.setAdapter(todayAdapter);

        binding.upcomingRv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        upcomingAdapter = new DoctorAppointmentAdapter(upcomingList, new DoctorAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onAccept(AppointmentModel appointment) { updateAppointmentStatus(appointment, "Confirmed"); }
            @Override
            public void onReject(AppointmentModel appointment) { showRejectionDialog(appointment); }
            @Override
            public void onDetails(AppointmentModel appointment) { navigateToDetails(appointment); }
            @Override
            public void onStartConsultation(AppointmentModel appointment) { startConsultation(appointment); }
        });
        binding.upcomingRv.setAdapter(upcomingAdapter);

        // Navigation
        binding.navAppointments.setOnClickListener(v -> startActivity(new Intent(this, DoctorAppointmentsActivity.class)));
        binding.navAvailability.setOnClickListener(v -> startActivity(new Intent(this, DoctorAvailabilityActivity.class)));
        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorProfileActivity.class);
            intent.putExtra("doctor", currentDoctor);
            startActivity(intent);
        });
        binding.navPatients.setOnClickListener(v -> {
            // startActivity(new Intent(this, DoctorPatientsActivity.class));
        });
        binding.navDashboard.setOnClickListener(v -> {}); // Already here

        binding.todayCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAppointmentsActivity.class);
            intent.putExtra("filter", "Today");
            startActivity(intent);
        });

        binding.pendingCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAppointmentsActivity.class);
            intent.putExtra("filter", "Pending");
            startActivity(intent);
        });

        binding.completedCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorEarningsActivity.class);
            startActivity(intent);
        });

        binding.cancelledCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAppointmentsActivity.class);
            intent.putExtra("filter", "Cancelled");
            startActivity(intent);
        });

        binding.notificationLayout.setOnClickListener(v -> {
            // startActivity(new Intent(this, DoctorNotificationsActivity.class));
        });

        binding.viewAllTodayBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAppointmentsActivity.class);
            intent.putExtra("filter", "Today");
            startActivity(intent);
        });
    }

    private void updateAppointmentStatus(AppointmentModel app, String status) {
        FirebaseHelper.getAppointmentsRef().child(app.getAppointmentId()).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show());
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
                    updateAppointmentStatus(app, "Rejected");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToDetails(AppointmentModel app) {
        Intent intent = new Intent(this, DoctorAppointmentDetailActivity.class);
        intent.putExtra("appointment", app);
        startActivity(intent);
    }

    private void startConsultation(AppointmentModel app) {
        // Intent intent = new Intent(this, ConsultationActivity.class);
        // intent.putExtra("appointment", app);
        // startActivity(intent);
    }

    private void loadDoctorData() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getDoctorsRef().orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot doctorSnap = snapshot.getChildren().iterator().next();
                    currentDoctor = doctorSnap.getValue(DoctorsModel.class);
                    currentDoctor.setKey(doctorSnap.getKey());
                    updateDoctorUI();
                    loadAppointments();
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(DoctorMainActivity.this, "Doctor profile not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { binding.progressBar.setVisibility(View.GONE); }
        });
    }

    private void updateDoctorUI() {
        binding.doctorNameTxt.setText("Dr. " + currentDoctor.getName());
        binding.greetingTxt.setText(getGreeting() + ",");
        Glide.with(this).load(currentDoctor.getPicture()).placeholder(R.drawable.profile).into(binding.doctorProfileImg);
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private void loadAppointments() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String todayDate = sdf.format(new Date());

        Calendar cal = Calendar.getInstance();
        List<String> next7Days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            next7Days.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        FirebaseHelper.getAppointmentsRef().orderByChild("doctorId").equalTo(currentDoctor.getKey())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        todayList.clear();
                        upcomingList.clear();
                        int pending = 0, completed = 0, cancelled = 0, today = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            AppointmentModel app = ds.getValue(AppointmentModel.class);
                            if (app == null) continue;

                            String status = app.getStatus();
                            String date = app.getAppointmentDate();

                            if ("Pending".equals(status)) pending++;
                            else if ("Completed".equals(status)) completed++;
                            else if ("Cancelled".equals(status) || "Rejected".equals(status)) cancelled++;

                            if (date != null) {
                                if (date.equals(todayDate)) {
                                    today++;
                                    todayList.add(app);
                                } else if (next7Days.contains(date)) {
                                    upcomingList.add(app);
                                }
                            }
                        }

                        binding.todayCountTxt.setText(String.valueOf(today));
                        binding.pendingCountTxt.setText(String.valueOf(pending));
                        binding.completedCountTxt.setText(String.valueOf(completed));
                        binding.cancelledCountTxt.setText(String.valueOf(cancelled));

                        todayAdapter.notifyDataSetChanged();
                        upcomingAdapter.notifyDataSetChanged();

                        binding.emptyTodayTxt.setVisibility(todayList.isEmpty() ? View.VISIBLE : View.GONE);
                        binding.emptyUpcomingTxt.setVisibility(upcomingList.isEmpty() ? View.VISIBLE : View.GONE);
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { binding.progressBar.setVisibility(View.GONE); }
                });
    }
}
