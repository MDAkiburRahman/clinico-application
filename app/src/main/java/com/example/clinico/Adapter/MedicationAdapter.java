package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.Domain.MedicationModel;
import com.example.clinico.databinding.ViewholderMedicationBinding;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private List<MedicationModel> items;
    private OnMedicationActionListener listener;

    public interface OnMedicationActionListener {
        void onDelete(MedicationModel model);
        void onStatusChange(MedicationModel model, boolean isTaken);
    }

    public MedicationAdapter(List<MedicationModel> items) {
        this.items = items;
    }

    public MedicationAdapter(List<MedicationModel> items, OnMedicationActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderMedicationBinding binding = ViewholderMedicationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationModel item = items.get(position);
        holder.binding.medNameTxt.setText(item.getName());
        holder.binding.medDosageTxt.setText(item.getDosage());
        holder.binding.medTimeTxt.setText(item.getTime());
        holder.binding.medTakenCb.setOnCheckedChangeListener(null); // Reset to prevent recursion
        holder.binding.medTakenCb.setChecked(item.isIsTaken());

        if (item.isIsTaken()) {
            holder.binding.medNameTxt.setPaintFlags(holder.binding.medNameTxt.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.binding.getRoot().setAlpha(0.6f);
        } else {
            holder.binding.medNameTxt.setPaintFlags(holder.binding.medNameTxt.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.binding.getRoot().setAlpha(1.0f);
        }

        holder.binding.medTakenCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setIsTaken(isChecked);
            if (isChecked) {
                item.setLastTakenTimestamp(System.currentTimeMillis());
            } else {
                item.setLastTakenTimestamp(0);
            }

            if (listener != null) {
                listener.onStatusChange(item, isChecked);
            } else {
                com.google.firebase.database.DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference(com.example.clinico.Utils.Constants.NODE_MEDICATIONS)
                        .child(item.getId());
                
                java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                updates.put("isTaken", item.isIsTaken());
                updates.put("lastTakenTimestamp", item.getLastTakenTimestamp());
                ref.updateChildren(updates);
            }
            
            // Trigger visual update
            notifyItemChanged(position);
        });

        if (listener != null) {
            holder.binding.medDeleteBtn.setVisibility(android.view.View.VISIBLE);
            holder.binding.medDeleteBtn.setOnClickListener(v -> listener.onDelete(item));
        } else {
            holder.binding.medDeleteBtn.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderMedicationBinding binding;
        public ViewHolder(ViewholderMedicationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
