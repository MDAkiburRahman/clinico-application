package com.example.clinico.Adapter;

import android.content.Context;
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

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private final List<AppointmentModel> items;
    private final AppointmentActionListener listener;

    public interface AppointmentActionListener {
        void onCancel(AppointmentModel appointment);
        void onDelete(AppointmentModel appointment);
        void onDetails(AppointmentModel appointment);
        void onReview(AppointmentModel appointment);
        void onComplete(AppointmentModel appointment);
    }

    public AppointmentAdapter(List<AppointmentModel> items, AppointmentActionListener listener) {
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
        holder.binding.doctorNameTxt.setText(item.getDoctorName());
        holder.binding.specializationTxt.setText(item.getSpecialization());
        holder.binding.dateTimeTxt.setText(item.getAppointmentDate() + " | " + item.getAppointmentTime());
        holder.binding.statusTxt.setText(item.getStatus());

        if (item.getStatus().equals("Rejected") && item.getRejectionNote() != null && !item.getRejectionNote().isEmpty()) {
            holder.binding.rejectionNoteTxt.setVisibility(android.view.View.VISIBLE);
            holder.binding.rejectionNoteTxt.setText("Reason: " + item.getRejectionNote());
        } else {
            holder.binding.rejectionNoteTxt.setVisibility(android.view.View.GONE);
        }

        // Color status
        switch (item.getStatus()) {
            case "Pending":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "Confirmed":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "Cancelled":
            case "Rejected":
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#F44336"));
                break;
            default:
                holder.binding.statusTxt.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
                holder.binding.statusTxt.setTextColor(Color.parseColor("#2196F3"));
                break;
        }

        Glide.with(holder.itemView.getContext())
                .load(item.getDoctorImage())
                .placeholder(R.drawable.ic_person)
                .into(holder.binding.doctorImg);

        holder.binding.optionsBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.appointment_options, popup.getMenu());
            
            if (!item.getStatus().equals("Pending") && !item.getStatus().equals("Confirmed")) {
                popup.getMenu().findItem(R.id.action_cancel).setVisible(false);
            }

            if (!item.getStatus().equals("Confirmed")) {
                popup.getMenu().findItem(R.id.action_complete).setVisible(false);
            }
            
            if (item.getStatus().equals("Pending") || item.getStatus().equals("Confirmed")) {
                popup.getMenu().findItem(R.id.action_delete).setVisible(false);
            }

            if (!item.getStatus().equals("Completed") || item.isReviewed()) {
                popup.getMenu().findItem(R.id.action_review).setVisible(false);
            }

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.action_cancel) {
                    listener.onCancel(item);
                    return true;
                } else if (menuItem.getItemId() == R.id.action_delete) {
                    listener.onDelete(item);
                    return true;
                } else if (menuItem.getItemId() == R.id.action_details) {
                    listener.onDetails(item);
                    return true;
                } else if (menuItem.getItemId() == R.id.action_review) {
                    listener.onReview(item);
                    return true;
                } else if (menuItem.getItemId() == R.id.action_complete) {
                    listener.onComplete(item);
                    return true;
                }
                return false;
            });
            popup.show();
        });
        
        holder.itemView.setOnClickListener(v -> listener.onDetails(item));
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
