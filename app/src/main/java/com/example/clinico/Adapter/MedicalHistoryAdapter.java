package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.MedicalRecordModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderMedicalRecordBinding;

import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {

    private final List<MedicalRecordModel> items;

    public MedicalHistoryAdapter(List<MedicalRecordModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderMedicalRecordBinding binding = ViewholderMedicalRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalRecordModel item = items.get(position);
        holder.binding.doctorNameTxt.setText(item.getDoctorName());
        holder.binding.dateTxt.setText(item.getDate());
        holder.binding.diagnosisTxt.setText("Diagnosis: " + item.getDiagnosis());
        holder.binding.prescriptionTxt.setText("Prescription: " + item.getPrescription());
        holder.binding.notesTxt.setText("Doctor's Notes: " + item.getDoctorNotes());

        if (item.getReportImageUrl() != null && !item.getReportImageUrl().isEmpty()) {
            holder.binding.reportImg.setVisibility(View.VISIBLE);
            
            if (item.getReportImageUrl().toLowerCase().endsWith(".pdf")) {
                holder.binding.reportImg.setImageResource(R.drawable.empty_box); // Placeholder for PDF
                // You might want to use a specific PDF icon here
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(item.getReportImageUrl())
                        .into(holder.binding.reportImg);
            }
            
            holder.binding.reportImg.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(item.getReportImageUrl()));
                holder.itemView.getContext().startActivity(intent);
            });
        } else {
            holder.binding.reportImg.setVisibility(View.GONE);
        }

        holder.binding.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });

        holder.binding.viewDetailsBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(item);
            }
        });
    }

    private OnRecordActionListener listener;

    public interface OnRecordActionListener {
        void onDelete(MedicalRecordModel record);
        void onViewDetails(MedicalRecordModel record);
    }

    public void setOnRecordActionListener(OnRecordActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderMedicalRecordBinding binding;

        public ViewHolder(ViewholderMedicalRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
