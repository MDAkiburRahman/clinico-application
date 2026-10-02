package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.clinico.Adapter.MedicationAdapter;
import com.example.clinico.Domain.MedicationModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityMedicationBinding;
import com.example.clinico.databinding.DialogAddMedicationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MedicationActivity extends BaseActivity {
    private ActivityMedicationBinding binding;
    private List<MedicationModel> medList = new ArrayList<>();
    private MedicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadMedications();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.medicationRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicationAdapter(medList, new MedicationAdapter.OnMedicationActionListener() {
            @Override
            public void onDelete(MedicationModel model) {
                deleteMedication(model);
            }

            @Override
            public void onStatusChange(MedicationModel model, boolean isTaken) {
                java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                updates.put("isTaken", isTaken);
                updates.put("lastTakenTimestamp", model.getLastTakenTimestamp());
                database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS).child(model.getId()).updateChildren(updates);
            }
        });
        binding.medicationRv.setAdapter(adapter);

        binding.addMedFab.setOnClickListener(v -> showAddMedicationDialog());
    }

    private void loadMedications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseHelper.getUid();
        if (uid == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS).orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        medList.clear();
                        android.util.Log.d("MedicationActivity", "Data count: " + snapshot.getChildrenCount());
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MedicationModel model = ds.getValue(MedicationModel.class);
                            if (model != null) {
                                model.setId(ds.getKey());
                                if (model.shouldReset()) {
                                    model.setIsTaken(false);
                                    model.setLastTakenTimestamp(0);
                                    database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS)
                                            .child(model.getId()).child("isTaken").setValue(false);
                                    database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS)
                                            .child(model.getId()).child("lastTakenTimestamp").setValue(0);
                                }
                                medList.add(model);
                                android.util.Log.d("MedicationActivity", "Added: " + model.getName());
                            } else {
                                android.util.Log.e("MedicationActivity", "Failed to map: " + ds.getValue());
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyTxt.setVisibility(medList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        android.util.Log.e("MedicationActivity", "Error: " + error.getMessage());
                    }
                });
    }

    private void showAddMedicationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddMedicationBinding b = DialogAddMedicationBinding.inflate(getLayoutInflater());
        builder.setView(b.getRoot());

        b.medTimeEt.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = calendar.get(java.util.Calendar.MINUTE);

            new android.app.TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                String time = String.format("%02d:%02d %s",
                        (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12,
                        minuteOfHour,
                        hourOfDay < 12 ? "AM" : "PM");
                b.medTimeEt.setText(time);
            }, hour, minute, false).show();
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = b.medNameEt.getText().toString().trim();
            String dosage = b.medDosageEt.getText().toString().trim();
            String time = b.medTimeEt.getText().toString().trim();
            String frequency = b.medFrequencyEt.getText().toString().trim();
            String notes = b.medNotesEt.getText().toString().trim();

            if (!name.isEmpty() && !dosage.isEmpty() && !time.isEmpty()) {
                addMedication(name, dosage, time, frequency, notes);
            } else {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addMedication(String name, String dosage, String time, String frequency, String notes) {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        String id = database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS).push().getKey();
        if (id == null) return;

        MedicationModel med = new MedicationModel(id, name, dosage, time, frequency, notes, uid);
        database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS).child(id).setValue(med).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Medication added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMedication(MedicationModel model) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to remove " + model.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS).child(model.getId()).removeValue();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
