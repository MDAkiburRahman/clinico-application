package com.example.clinico.Activity;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.MedicalHistoryAdapter;
import com.example.clinico.Domain.MedicalRecordModel;
import com.example.clinico.Helper.CloudinaryHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityMedicalHistoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicalHistoryActivity extends BaseActivity {
    private ActivityMedicalHistoryBinding binding;
    private MedicalHistoryAdapter adapter;
    private List<MedicalRecordModel> recordList = new ArrayList<>();
    private Uri selectedFileUri;
    private TextView fileNameTxt;

    private final ActivityResultLauncher<String[]> getFile = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    if (fileNameTxt != null) {
                        fileNameTxt.setText("File selected: " + uri.getLastPathSegment());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicalHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CloudinaryHelper.init(this);
        initView();
        loadMedicalHistory();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.medicalHistoryRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicalHistoryAdapter(recordList);
        adapter.setOnRecordActionListener(new MedicalHistoryAdapter.OnRecordActionListener() {
            @Override
            public void onDelete(MedicalRecordModel record) {
                confirmDelete(record);
            }

            @Override
            public void onViewDetails(MedicalRecordModel record) {
                // Implement view details if needed, for now just show a toast or open file
                if (record.getReportImageUrl() != null && !record.getReportImageUrl().isEmpty()) {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse(record.getReportImageUrl()));
                    startActivity(intent);
                } else {
                    Toast.makeText(MedicalHistoryActivity.this, "No report available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.medicalHistoryRv.setAdapter(adapter);

        binding.addRecordBtn.setOnClickListener(v -> showAddRecordDialog());
    }

    private void confirmDelete(MedicalRecordModel record) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medical Record")
                .setMessage("Are you sure you want to delete this medical record?")
                .setPositiveButton("Delete", (dialog, which) -> deleteRecord(record))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRecord(MedicalRecordModel record) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        if (record.getRecordId().startsWith("APP_")) {
            // This is a completed appointment. 
            // The requirement says deleting a medical record must NOT accidentally delete the original appointment.
            // We can mark it as "hidden" from medical history for this user.
            database.getReference("Appointments")
                    .child(record.getRecordId().substring(4))
                    .child("hiddenFromHistory")
                    .setValue(true)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Record removed from history", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // This is a manual medical record
            database.getReference("MedicalHistory")
                    .child(uid)
                    .child(record.getRecordId())
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showAddRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_medical_record, null);
        builder.setView(view);

        EditText doctorNameEt = view.findViewById(R.id.doctorNameEt);
        EditText diagnosisEt = view.findViewById(R.id.diagnosisEt);
        EditText notesEt = view.findViewById(R.id.notesEt);
        fileNameTxt = view.findViewById(R.id.fileNameTxt);
        androidx.appcompat.widget.AppCompatButton selectFileBtn = view.findViewById(R.id.selectFileBtn);
        androidx.appcompat.widget.AppCompatButton submitBtn = view.findViewById(R.id.submitBtn);

        AlertDialog dialog = builder.create();

        selectFileBtn.setOnClickListener(v -> {
            getFile.launch(new String[]{"image/*", "application/pdf"});
        });

        submitBtn.setOnClickListener(v -> {
            String doctorName = doctorNameEt.getText().toString();
            String diagnosis = diagnosisEt.getText().toString();
            String notes = notesEt.getText().toString();

            if (doctorName.isEmpty() || diagnosis.isEmpty() || selectedFileUri == null) {
                Toast.makeText(this, "Please fill required fields and select a file", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadAndSaveRecord(doctorName, diagnosis, notes, dialog);
        });

        dialog.show();
    }

    private void uploadAndSaveRecord(String doctorName, String diagnosis, String notes, AlertDialog dialog) {
        binding.progressBar.setVisibility(View.VISIBLE);
        CloudinaryHelper.uploadFile(selectedFileUri, new CloudinaryHelper.UploadListener() {
            @Override
            public void onSuccess(String url) {
                saveToFirebase(doctorName, diagnosis, notes, url, dialog);
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MedicalHistoryActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFirebase(String doctorName, String diagnosis, String notes, String fileUrl, AlertDialog dialog) {
        String uid = mAuth.getCurrentUser().getUid();
        String recordId = database.getReference("MedicalHistory").child(uid).push().getKey();
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        MedicalRecordModel record = new MedicalRecordModel(
                recordId, uid, "", doctorName, diagnosis, "", fileUrl, notes, date, System.currentTimeMillis()
        );

        if (recordId != null) {
            database.getReference("MedicalHistory").child(uid).child(recordId).setValue(record)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(MedicalHistoryActivity.this, "Record added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            selectedFileUri = null;
                        } else {
                            Toast.makeText(MedicalHistoryActivity.this, "Failed to save record", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadMedicalHistory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        // Load manual records
        database.getReference("MedicalHistory").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MedicalRecordModel> manualRecords = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            MedicalRecordModel record = data.getValue(MedicalRecordModel.class);
                            if (record != null) {
                                manualRecords.add(record);
                            }
                        }
                        fetchCompletedAppointments(manualRecords);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void fetchCompletedAppointments(List<MedicalRecordModel> manualRecords) {
        String uid = mAuth.getCurrentUser().getUid();
        database.getReference("Appointments")
                .orderByChild("userId")
                .equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recordList.clear();
                        recordList.addAll(manualRecords);

                        for (DataSnapshot data : snapshot.getChildren()) {
                            com.example.clinico.Domain.AppointmentModel app = data.getValue(com.example.clinico.Domain.AppointmentModel.class);
                            if (app != null && "Completed".equals(app.getStatus())) {
                                // Check if user has hidden this from history
                                Boolean isHidden = data.child("hiddenFromHistory").getValue(Boolean.class);
                                if (isHidden != null && isHidden) continue;

                                MedicalRecordModel record = new MedicalRecordModel(
                                        "APP_" + app.getAppointmentId(),
                                        app.getUserId(),
                                        app.getDoctorId(),
                                        app.getDoctorName(),
                                        app.getSpecialization(),
                                        "N/A", // Prescription might be in another node
                                        app.getDoctorImage(), // Using doctor image as placeholder or report
                                        "Consultation completed.",
                                        app.getAppointmentDate(),
                                        0 // Should ideally parse timestamp
                                );
                                recordList.add(record);
                            }
                        }

                        // Sort by date/timestamp if possible
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);

                        if (recordList.isEmpty()) {
                            binding.noDataTxt.setVisibility(View.VISIBLE);
                        } else {
                            binding.noDataTxt.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
