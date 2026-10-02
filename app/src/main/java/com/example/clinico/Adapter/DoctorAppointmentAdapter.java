package com.example.clinico.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.UserModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderDoctorAppointmentBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.ViewHolder> {

    private List<AppointmentModel> items;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onAccept(AppointmentModel appointment);
        void onReject(AppointmentModel appointment);
        void onDetails(AppointmentModel appointment);
        void onStartConsultation(AppointmentModel appointment);
    }

    public DoctorAppointmentAdapter(List<AppointmentModel> items, OnAppointmentActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderDoctorAppointmentBinding binding = ViewholderDoctorAppointmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.timeTxt.setText(item.getAppointmentTime());
        holder.binding.typeTxt.setText(item.getAppointmentType());
        holder.binding.paymentTxt.setText(item.getPaymentStatus());
        holder.binding.statusTxt.setText(item.getStatus());

        if (item.getUserName() != null) {
            holder.binding.patientNameTxt.setText(item.getUserName());
            holder.binding.patientInfoTxt.setText("Age: " + item.getUserAge() + " | " + item.getUserGender());
        }

        // Load Patient Info (Fallback or Refresh)
        FirebaseDatabase.getInstance().getReference("Users").child(item.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            holder.binding.patientNameTxt.setText(user.getName());
                            holder.binding.patientInfoTxt.setText("Phone: " + user.getPhone());
                            Glide.with(context).load(user.getProfilePic()).placeholder(R.drawable.profile).into(holder.binding.patientImg);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Status Colors
        switch (item.getStatus()) {
            case "Pending":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#FF9800"));
                holder.binding.acceptBtn.setVisibility(View.VISIBLE);
                holder.binding.rejectBtn.setVisibility(View.VISIBLE);
                holder.binding.startBtn.setVisibility(View.GONE);
                break;
            case "Confirmed":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#4CAF50"));
                holder.binding.acceptBtn.setVisibility(View.GONE);
                holder.binding.rejectBtn.setVisibility(View.VISIBLE);
                holder.binding.startBtn.setVisibility(View.VISIBLE);
                break;
            case "Cancelled":
            case "Rejected":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#F44336"));
                holder.binding.acceptBtn.setVisibility(View.GONE);
                holder.binding.rejectBtn.setVisibility(View.GONE);
                holder.binding.startBtn.setVisibility(View.GONE);
                break;
            case "Completed":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#2196F3"));
                holder.binding.acceptBtn.setVisibility(View.GONE);
                holder.binding.rejectBtn.setVisibility(View.GONE);
                holder.binding.startBtn.setVisibility(View.GONE);
                break;
        }

        holder.binding.acceptBtn.setOnClickListener(v -> listener.onAccept(item));
        holder.binding.rejectBtn.setOnClickListener(v -> listener.onReject(item));
        holder.binding.viewDetailsBtn.setOnClickListener(v -> listener.onDetails(item));
        holder.binding.startBtn.setOnClickListener(v -> listener.onStartConsultation(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderDoctorAppointmentBinding binding;

        public ViewHolder(ViewholderDoctorAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
