package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityPaymentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class PaymentActivity extends BaseActivity {
    private ActivityPaymentBinding binding;
    private AppointmentModel appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appointment = (AppointmentModel) getIntent().getSerializableExtra("appointment");
        if (appointment == null) {
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.summaryDoctorTxt.setText("Doctor: " + appointment.getDoctorName());
        binding.summaryDateTimeTxt.setText("Date: " + appointment.getAppointmentDate() + " | Time: " + appointment.getAppointmentTime());
        binding.summaryFeeTxt.setText("৳" + appointment.getConsultationFee());

        binding.confirmBtn.setOnClickListener(v -> confirmBooking());
    }

    private void confirmBooking() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.confirmBtn.setEnabled(false);

        String paymentMethod = "bKash";
        int checkedId = binding.paymentMethodGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.methodNagad) paymentMethod = "Nagad";
        else if (checkedId == R.id.methodHospital) paymentMethod = "Pay at Hospital";

        appointment.setPaymentMethod(paymentMethod);
        appointment.setStatus("Pending");
        appointment.setBookingTime(System.currentTimeMillis());

        // Fetch User Details to store in Appointment for Doctor's view
        FirebaseDatabase.getInstance().getReference("Users").child(appointment.getUserId())
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                com.example.clinico.Domain.UserModel user = snapshot.getValue(com.example.clinico.Domain.UserModel.class);
                if (user != null) {
                    appointment.setUserName(user.getName());
                    appointment.setUserAge(calculateAge(user.getDob()));
                    appointment.setUserGender(user.getGender());
                }
                saveAppointment();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                saveAppointment();
            }
        });
    }

    private String calculateAge(String dob) {
        if (dob == null || dob.isEmpty()) return "N/A";
        try {
            // Assumes dob format "dd/MM/yyyy" as per ProfileActivity
            String[] parts = dob.split("/");
            int year = Integer.parseInt(parts[2]);
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            return String.valueOf(currentYear - year);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void saveAppointment() {
        FirebaseDatabase.getInstance().getReference("Appointments")
                .child(appointment.getAppointmentId())
                .setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    showSuccessDialog(appointment);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.confirmBtn.setEnabled(true);
                    Toast.makeText(this, "Booking failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog(AppointmentModel appointment) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_booking_success, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        TextView msg = view.findViewById(R.id.successMsg);
        msg.setText("Your appointment with " + appointment.getDoctorName() + " has been successfully scheduled.");

        view.findViewById(R.id.viewAppBtn).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(PaymentActivity.this, MyAppointmentsActivity.class));
            finish();
        });

        view.findViewById(R.id.homeBtn).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}
