package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.UserModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityDoctorAppointmentDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DoctorAppointmentDetailActivity extends BaseActivity {
    private ActivityDoctorAppointmentDetailBinding binding;
    private AppointmentModel appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorAppointmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appointment = (AppointmentModel) getIntent().getSerializableExtra("appointment");
        if (appointment == null) {
            finish();
            return;
        }

        initView();
        loadPatientData();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.apptIdTxt.setText("#" + appointment.getAppointmentId().substring(Math.max(0, appointment.getAppointmentId().length() - 5)));
        binding.apptDateTimeTxt.setText(appointment.getAppointmentDate() + " | " + appointment.getAppointmentTime());
        binding.apptTypeTxt.setText(appointment.getAppointmentType());
        binding.apptPaymentTxt.setText(appointment.getPaymentStatus() + " (" + appointment.getPaymentMethod() + ")");
        binding.apptStatusTxt.setText(appointment.getStatus());
        // binding.apptReasonTxt.setText(appointment.getReason()); // Add reason to model if needed

        updateActionButtons();

        binding.acceptBtn.setOnClickListener(v -> updateStatus("Confirmed"));
        binding.rejectBtn.setOnClickListener(v -> showRejectionDialog());
        binding.completeBtn.setOnClickListener(v -> updateStatus("Completed"));
        binding.cancelBtn.setOnClickListener(v -> updateStatus("Cancelled"));
        
        binding.startBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsultationActivity.class);
            intent.putExtra("appointment", appointment);
            startActivity(intent);
        });

        binding.historyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientHistoryActivity.class);
            intent.putExtra("patientId", appointment.getUserId());
            startActivity(intent);
        });
    }

    private void updateActionButtons() {
        String status = appointment.getStatus();
        binding.acceptBtn.setVisibility("Pending".equals(status) ? View.VISIBLE : View.GONE);
        binding.rejectBtn.setVisibility("Pending".equals(status) || "Confirmed".equals(status) ? View.VISIBLE : View.GONE);
        binding.startBtn.setVisibility("Confirmed".equals(status) ? View.VISIBLE : View.GONE);
        binding.completeBtn.setVisibility("Confirmed".equals(status) ? View.VISIBLE : View.GONE);
        binding.cancelBtn.setVisibility("Confirmed".equals(status) ? View.VISIBLE : View.GONE);
    }

    private void updateStatus(String status) {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getAppointmentsRef().child(appointment.getAppointmentId()).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    appointment.setStatus(status);
                    binding.apptStatusTxt.setText(status);
                    updateActionButtons();
                    Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }

    private void showRejectionDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Reason for rejection");
        new android.app.AlertDialog.Builder(this)
                .setTitle("Reject Appointment")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = input.getText().toString();
                    FirebaseHelper.getAppointmentsRef().child(appointment.getAppointmentId()).child("rejectionNote").setValue(reason);
                    updateStatus("Rejected");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadPatientData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getUsersRef().child(appointment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    binding.patientNameTxt.setText(user.getName());
                    binding.patientMetaTxt.setText("DOB: " + user.getDob());
                    binding.patientPhoneTxt.setText("Phone: " + user.getPhone());
                    binding.patientEmailTxt.setText("Email: " + user.getEmail());
                    Glide.with(DoctorAppointmentDetailActivity.this).load(user.getProfilePic()).placeholder(R.drawable.profile).into(binding.patientImg);
                }
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}
