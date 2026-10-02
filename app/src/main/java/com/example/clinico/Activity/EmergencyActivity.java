package com.example.clinico.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AmbulanceAdapter;
import com.example.clinico.Domain.EmergencyContactModel;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityEmergencyBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.example.clinico.R;
import java.util.ArrayList;
import java.util.List;

public class EmergencyActivity extends BaseActivity {
    private ActivityEmergencyBinding binding;
    private List<EmergencyContactModel> ambulanceList = new ArrayList<>();
    private AmbulanceAdapter adapter;
    private EmergencyContactModel personalContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadEmergencyContacts();
        loadAmbulances();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        boolean isAdmin = Constants.ROLE_ADMIN.equals(sessionManager.getUserRole());
        
        binding.ambulanceRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AmbulanceAdapter(ambulanceList, isAdmin, new AmbulanceAdapter.OnAmbulanceActionListener() {
            @Override
            public void onDelete(EmergencyContactModel item) {
                database.getReference("AppConfig").child("Ambulances").child(item.getId()).removeValue();
            }

            @Override
            public void onEdit(EmergencyContactModel item) {
                showAmbulanceDialog(item);
            }
        });
        binding.ambulanceRv.setAdapter(adapter);

        if (isAdmin) {
            binding.addAmbulanceBtn.setVisibility(View.VISIBLE);
            binding.addAmbulanceBtn.setOnClickListener(v -> showAmbulanceDialog(null));
        }

        binding.saveContactBtn.setOnClickListener(v -> {
            String name = binding.contactNameEt.getText().toString();
            String phone = binding.contactPhoneEt.getText().toString();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            savePersonalContact(name, phone);
        });

        binding.callContactBtn.setOnClickListener(v -> {
            if (personalContact != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + personalContact.getPhone()));
                startActivity(intent);
            }
        });
    }

    private void showAmbulanceDialog(EmergencyContactModel item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_ambulance, null);
        EditText nameEt = view.findViewById(R.id.ambNameEt);
        EditText phoneEt = view.findViewById(R.id.ambPhoneEt);

        if (item != null) {
            nameEt.setText(item.getName());
            phoneEt.setText(item.getPhone());
        }

        new AlertDialog.Builder(this)
                .setTitle(item == null ? "Add Ambulance" : "Edit Ambulance")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameEt.getText().toString();
                    String phone = phoneEt.getText().toString();
                    if (!name.isEmpty() && !phone.isEmpty()) {
                        saveAmbulance(item != null ? item.getId() : null, name, phone);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveAmbulance(String id, String name, String phone) {
        if (id == null) {
            id = database.getReference("AppConfig").child("Ambulances").push().getKey();
        }
        EmergencyContactModel ambulance = new EmergencyContactModel(id, name, phone, "Ambulance");
        database.getReference("AppConfig").child("Ambulances").child(id).setValue(ambulance);
    }

    private void loadAmbulances() {
        database.getReference("AppConfig").child("Ambulances").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ambulanceList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    EmergencyContactModel model = ds.getValue(EmergencyContactModel.class);
                    if (model != null) {
                        ambulanceList.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadEmergencyContacts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Users").child(mAuth.getCurrentUser().getUid()).child("emergencyContact")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            personalContact = snapshot.getValue(EmergencyContactModel.class);
                            if (personalContact != null) {
                                binding.contactNameEt.setText(personalContact.getName());
                                binding.contactPhoneEt.setText(personalContact.getPhone());
                                binding.callContactBtn.setVisibility(View.VISIBLE);
                                binding.callContactBtn.setText("Call " + personalContact.getName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void savePersonalContact(String name, String phone) {
        binding.progressBar.setVisibility(View.VISIBLE);
        EmergencyContactModel contact = new EmergencyContactModel("personal", name, phone, "Emergency");
        database.getReference("Users").child(mAuth.getCurrentUser().getUid()).child("emergencyContact")
                .setValue(contact).addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save contact", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
