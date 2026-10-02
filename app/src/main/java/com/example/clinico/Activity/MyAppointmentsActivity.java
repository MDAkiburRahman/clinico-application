package com.example.clinico.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AppointmentAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Domain.ReviewModel;
import com.example.clinico.Domain.UserModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityMyAppointmentsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyAppointmentsActivity extends BaseActivity {
    private ActivityMyAppointmentsBinding binding;
    private AppointmentAdapter adapter;
    private List<AppointmentModel> allAppointments = new ArrayList<>();
    private List<AppointmentModel> filteredAppointments = new ArrayList<>();
    private String currentStatus = "Pending"; // Matches first tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadAppointments();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.appointmentRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(filteredAppointments, new AppointmentAdapter.AppointmentActionListener() {
            @Override
            public void onCancel(AppointmentModel appointment) {
                cancelAppointment(appointment);
            }

            @Override
            public void onDelete(AppointmentModel appointment) {
                deleteAppointment(appointment);
            }

            @Override
            public void onDetails(AppointmentModel appointment) {
                navigateToDoctorDetails(appointment.getDoctorId());
            }

            @Override
            public void onReview(AppointmentModel appointment) {
                showReviewDialog(appointment);
            }

            @Override
            public void onComplete(AppointmentModel appointment) {
                completeAppointment(appointment);
            }
        });
        binding.appointmentRv.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentStatus = "Pending"; break;
                    case 1: currentStatus = "Completed"; break;
                    case 2: currentStatus = "Cancelled"; break;
                }
                filterAppointments();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadAppointments);
    }

    private void loadAppointments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyTxt.setVisibility(View.GONE);
        
        database.getReference("Appointments")
                .orderByChild("userId")
                .equalTo(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allAppointments.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            AppointmentModel model = data.getValue(AppointmentModel.class);
                            if (model != null) {
                                allAppointments.add(model);
                            }
                        }
                        filterAppointments();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(MyAppointmentsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterAppointments() {
        filteredAppointments.clear();
        for (AppointmentModel app : allAppointments) {
            if (app.getStatus().equals(currentStatus) || 
               (currentStatus.equals("Pending") && app.getStatus().equals("Confirmed"))) {
                filteredAppointments.add(app);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredAppointments.isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
        }
    }

    private void navigateToDoctorDetails(String doctorId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Doctors").child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    DoctorsModel doctor = snapshot.getValue(DoctorsModel.class);
                    if (doctor != null) {
                        Intent intent = new Intent(MyAppointmentsActivity.this, DetailActivity.class);
                        intent.putExtra("object", doctor);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(MyAppointmentsActivity.this, "Doctor information not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyAppointmentsActivity.this, "Error fetching doctor details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAppointment(AppointmentModel appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to remove this appointment record from your history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    database.getReference("Appointments")
                            .child(appointment.getAppointmentId())
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                binding.progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Record Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void cancelAppointment(AppointmentModel appointment) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    database.getReference("Appointments")
                            .child(appointment.getAppointmentId())
                            .child("status")
                            .setValue("Cancelled")
                            .addOnCompleteListener(task -> {
                                binding.progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Appointment Cancelled", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to cancel", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void completeAppointment(AppointmentModel appointment) {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Appointments")
                .child(appointment.getAppointmentId())
                .child("status")
                .setValue("Completed")
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Appointment marked as completed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReviewDialog(AppointmentModel appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_review, null);
        builder.setView(view);

        RatingBar ratingBar = view.findViewById(R.id.dialogRatingBar);
        EditText reviewEt = view.findViewById(R.id.dialogReviewEt);
        androidx.appcompat.widget.AppCompatButton submitBtn = view.findViewById(R.id.dialogSubmitBtn);

        AlertDialog dialog = builder.create();

        submitBtn.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = reviewEt.getText().toString();

            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReview(appointment, rating, comment, dialog);
        });

        dialog.show();
    }

    private void submitReview(AppointmentModel appointment, float rating, String comment, AlertDialog dialog) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        // Check if review already exists for this appointment
        database.getReference("Reviews").orderByChild("appointmentId").equalTo(appointment.getAppointmentId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(MyAppointmentsActivity.this, "You have already reviewed this appointment", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        // Continue submitting review
                        database.getReference("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserModel user = snapshot.getValue(UserModel.class);
                                String userName = user != null ? user.getName() : "Anonymous";
                                String userImage = user != null ? user.getProfilePic() : "";

                                String reviewId = database.getReference("Reviews").push().getKey();
                                ReviewModel review = new ReviewModel(
                                        reviewId, uid, userName, userImage, appointment.getDoctorId(), rating, comment, System.currentTimeMillis(), appointment.getAppointmentId()
                                );

                                if (reviewId != null) {
                                    database.getReference("Reviews").child(reviewId).setValue(review).addOnCompleteListener(task -> {
                                        binding.progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MyAppointmentsActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
                                            database.getReference("Appointments")
                                                    .child(appointment.getAppointmentId())
                                                    .child("reviewed")
                                                    .setValue(true);
                                            dialog.dismiss();
                                            updateDoctorRating(appointment.getDoctorId());
                                        } else {
                                            Toast.makeText(MyAppointmentsActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                binding.progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void updateDoctorRating(String doctorId) {
        database.getReference("Reviews").orderByChild("doctorId").equalTo(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        float totalRating = 0;
                        int count = 0;
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ReviewModel review = data.getValue(ReviewModel.class);
                            if (review != null) {
                                totalRating += review.getRating();
                                count++;
                            }
                        }

                        if (count > 0) {
                            float average = totalRating / count;
                            database.getReference("Doctors").child(doctorId).child("Rating").setValue(average);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
