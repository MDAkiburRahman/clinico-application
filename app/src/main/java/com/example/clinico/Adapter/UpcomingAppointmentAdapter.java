package com.example.clinico.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderUpcomingAppointmentBinding;
import java.util.List;

public class UpcomingAppointmentAdapter extends RecyclerView.Adapter<UpcomingAppointmentAdapter.ViewHolder> {
    private List<AppointmentModel> items;
    private Context context;

    public UpcomingAppointmentAdapter(List<AppointmentModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderUpcomingAppointmentBinding binding = ViewholderUpcomingAppointmentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel item = items.get(position);
        holder.binding.doctorNameTxt.setText(item.getDoctorName());
        holder.binding.specializationTxt.setText(item.getSpecialization());
        holder.binding.dateTimeTxt.setText(item.getAppointmentTime() + " | " + item.getAppointmentDate());
        holder.binding.locationTxt.setText(item.getHospital());

        Glide.with(context)
                .load(item.getDoctorImage())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.binding.doctorImg);

        if ("Telehealth".equalsIgnoreCase(item.getHospital())) {
            holder.binding.actionBtn.setText("Call Doctor (Online)");
        } else {
            holder.binding.actionBtn.setText("Call Doctor");
        }

        holder.binding.callBtn.setOnClickListener(v -> {
            navigateToDoctorProfile(item.getDoctorId());
        });

        holder.binding.actionBtn.setOnClickListener(v -> {
            callDoctor(item.getDoctorId());
        });
    }

    private void callDoctor(String doctorId) {
        if (doctorId == null || doctorId.isEmpty()) return;
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Doctors")
                .child(doctorId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String mobile = snapshot.child("Mobile").getValue(String.class);
                    if (mobile != null && !mobile.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + mobile));
                        context.startActivity(intent);
                    } else {
                        android.widget.Toast.makeText(context, "Doctor's phone number not available", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void navigateToDoctorProfile(String doctorId) {
        if (doctorId == null || doctorId.isEmpty()) return;
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Doctors")
                .child(doctorId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    com.example.clinico.Domain.DoctorsModel doctor = snapshot.getValue(com.example.clinico.Domain.DoctorsModel.class);
                    if (doctor != null) {
                        doctor.setKey(snapshot.getKey());
                        Intent intent = new Intent(context, com.example.clinico.Activity.DetailActivity.class);
                        intent.putExtra("object", doctor);
                        context.startActivity(intent);
                    }
                } else {
                    android.widget.Toast.makeText(context, "Doctor not found", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderUpcomingAppointmentBinding binding;
        public ViewHolder(ViewholderUpcomingAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
