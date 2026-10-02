package com.example.clinico.Activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.PrescriptionMedicineAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.ConsultationModel;
import com.example.clinico.Domain.MedicinePrescriptionModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityConsultationBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ConsultationActivity extends BaseActivity {
    private ActivityConsultationBinding binding;
    private AppointmentModel appointment;
    private List<MedicinePrescriptionModel> medicineList = new ArrayList<>();
    private PrescriptionMedicineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsultationBinding.inflate(getLayoutInflater());
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

        binding.medicineRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PrescriptionMedicineAdapter(medicineList);
        binding.medicineRv.setAdapter(adapter);

        binding.addMedicineBtn.setOnClickListener(v -> {
            medicineList.add(new MedicinePrescriptionModel("", "", "", "", "", ""));
            adapter.notifyItemInserted(medicineList.size() - 1);
        });

        binding.followUpEt.setOnClickListener(v -> showDatePicker());

        binding.saveBtn.setOnClickListener(v -> saveConsultation());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            binding.followUpEt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveConsultation() {
        String symptoms = binding.symptomsEt.getText().toString().trim();
        String diagnosis = binding.diagnosisEt.getText().toString().trim();

        if (diagnosis.isEmpty()) {
            Toast.makeText(this, "Please enter a diagnosis", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveBtn.setEnabled(false);

        String consId = FirebaseHelper.getDatabase().getReference("Consultations").push().getKey();
        ConsultationModel consultation = new ConsultationModel(
                consId,
                appointment.getAppointmentId(),
                appointment.getDoctorId(),
                appointment.getUserId(),
                symptoms,
                diagnosis,
                binding.notesEt.getText().toString().trim(),
                binding.testsEt.getText().toString().trim(),
                binding.followUpEt.getText().toString(),
                medicineList,
                System.currentTimeMillis()
        );

        FirebaseHelper.getDatabase().getReference("Consultations").child(consId).setValue(consultation)
                .addOnSuccessListener(aVoid -> {
                    // Update appointment status to Completed
                    FirebaseHelper.getAppointmentsRef().child(appointment.getAppointmentId()).child("status").setValue("Completed")
                            .addOnSuccessListener(aVoid1 -> {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Consultation saved and Appointment Completed", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveBtn.setEnabled(true);
                    Toast.makeText(this, "Failed to save consultation", Toast.LENGTH_SHORT).show();
                });
    }
}
