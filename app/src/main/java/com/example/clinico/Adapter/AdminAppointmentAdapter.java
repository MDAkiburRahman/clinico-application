package com.example.clinico.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderAppointmentBinding;

import java.util.List;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {
    private final List<AppointmentModel> items;
    private final OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onStatusChange(AppointmentModel appointment, String newStatus);
        void onEdit(AppointmentModel appointment);
    }

    public AdminAppointmentAdapter(List<AppointmentModel> items, OnAppointmentActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAppointmentBinding binding = ViewholderAppointmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel item = items.get(position);
        holder.binding.doctorNameTxt.setText("Dr. " + item.getDoctorName());
        holder.binding.specializationTxt.setText(item.getSpecialization());
        holder.binding.dateTimeTxt.setText(item.getAppointmentDate() + " | " + item.getAppointmentTime());
        holder.binding.statusTxt.setText(item.getStatus());

        // Fetch patient name
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Users").child(item.getUserId())
                .child("name").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.binding.patientNameTxt.setText("Patient: " + snapshot.getValue(String.class));
                    holder.binding.patientNameTxt.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.patientNameTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });

        // Status coloring
        switch (item.getStatus()) {
            case "Pending":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "Confirmed":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#4CAF50"));
                break;
            default:
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#F44336"));
                break;
        }

        Glide.with(holder.itemView.getContext())
                .load(item.getDoctorImage())
                .placeholder(R.drawable.ic_person)
                .into(holder.binding.doctorImg);

        holder.binding.optionsBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), v);
            popup.getMenu().add("Edit Details");
            popup.getMenu().add("Edit Doctor Profile");
            popup.getMenu().add("Confirm");
            popup.getMenu().add("Complete");
            popup.getMenu().add("Reject");
            popup.getMenu().add("Cancel");

            popup.setOnMenuItemClickListener(menuItem -> {
                String title = menuItem.getTitle().toString();
                if ("Edit Details".equals(title)) {
                    listener.onEdit(item);
                } else if ("Edit Doctor Profile".equals(title)) {
                    navigateToDoctorEditor(holder.itemView.getContext(), item.getDoctorId());
                } else {
                    listener.onStatusChange(item, title);
                }
                return true;
            });
            popup.show();
        });
    }

    private void navigateToDoctorEditor(android.content.Context context, String doctorId) {
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Doctors").child(doctorId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    com.example.clinico.Domain.DoctorsModel doctor = snapshot.getValue(com.example.clinico.Domain.DoctorsModel.class);
                    if (doctor != null) {
                        doctor.setKey(snapshot.getKey());
                        android.content.Intent intent = new android.content.Intent(context, com.example.clinico.Activity.AddDoctorActivity.class);
                        intent.putExtra("doctor", doctor);
                        context.startActivity(intent);
                    }
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderAppointmentBinding binding;
        public ViewHolder(ViewholderAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
