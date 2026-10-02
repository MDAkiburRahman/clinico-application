package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AdminAppointmentAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityAdminAppointmentsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.app.AlertDialog;

public class AdminAppointmentsActivity extends BaseActivity {
    private ActivityAdminAppointmentsBinding binding;
    private List<AppointmentModel> appointmentList = new ArrayList<>();
    private AdminAppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadAllAppointments();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.appointmentRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAppointmentAdapter(appointmentList, new AdminAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onStatusChange(AppointmentModel appointment, String newStatus) {
                updateStatus(appointment, newStatus);
            }

            @Override
            public void onEdit(AppointmentModel appointment) {
                showEditDialog(appointment);
            }
        });
        binding.appointmentRv.setAdapter(adapter);
    }

    private void showEditDialog(AppointmentModel app) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_appointment, null);
        EditText dateEt = view.findViewById(R.id.dateEt);
        EditText timeEt = view.findViewById(R.id.timeEt);
        Spinner doctorSpinner = view.findViewById(R.id.doctorSpinner);
        doctorSpinner.setVisibility(View.VISIBLE);
        
        dateEt.setText(app.getAppointmentDate());
        timeEt.setText(app.getAppointmentTime());

        // Load Doctors for the spinner
        List<DoctorsModel> doctorsList = new ArrayList<>();
        List<String> doctorNames = new ArrayList<>();
        
        database.getReference("Doctors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int selectedIndex = -1;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    DoctorsModel doc = ds.getValue(DoctorsModel.class);
                    if (doc != null) {
                        doc.setKey(ds.getKey());
                        doctorsList.add(doc);
                        doctorNames.add("Dr. " + doc.getName());
                        if (doc.getKey() != null && doc.getKey().equals(app.getDoctorId())) {
                            selectedIndex = doctorNames.size() - 1;
                        }
                    }
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminAppointmentsActivity.this, 
                        android.R.layout.simple_spinner_item, doctorNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                doctorSpinner.setAdapter(adapter);
                if (selectedIndex != -1) {
                    doctorSpinner.setSelection(selectedIndex);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Appointment")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    app.setAppointmentDate(dateEt.getText().toString());
                    app.setAppointmentTime(timeEt.getText().toString());
                    
                    int pos = doctorSpinner.getSelectedItemPosition();
                    if (pos != -1) {
                        DoctorsModel selectedDoc = doctorsList.get(pos);
                        app.setDoctorId(selectedDoc.getKey());
                        app.setDoctorName(selectedDoc.getName());
                        app.setDoctorImage(selectedDoc.getPicture());
                        app.setSpecialization(selectedDoc.getSpecial());
                        app.setHospital(selectedDoc.getLocation());
                        app.setConsultationFee(selectedDoc.getPrice());
                    }
                    
                    updateAppointment(app);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateAppointment(AppointmentModel app) {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Appointments").child(app.getAppointmentId()).setValue(app)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Appointment Updated", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAllAppointments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    AppointmentModel model = data.getValue(AppointmentModel.class);
                    if (model != null) {
                        appointmentList.add(model);
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

    private void updateStatus(AppointmentModel appointment, String newStatus) {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Appointments")
                .child(appointment.getAppointmentId())
                .child("status")
                .setValue(newStatus)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Status Updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
