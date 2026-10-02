package com.example.clinico.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinico.Domain.MedicinePrescriptionModel;
import com.example.clinico.databinding.ViewholderMedicineRowBinding;

import java.util.List;

public class PrescriptionMedicineAdapter extends RecyclerView.Adapter<PrescriptionMedicineAdapter.ViewHolder> {

    private List<MedicinePrescriptionModel> items;

    public PrescriptionMedicineAdapter(List<MedicinePrescriptionModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderMedicineRowBinding binding = ViewholderMedicineRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicinePrescriptionModel item = items.get(position);

        holder.binding.medicineNameEt.setText(item.getMedicineName());
        holder.binding.dosageEt.setText(item.getDosage());
        holder.binding.frequencyEt.setText(item.getFrequency());
        holder.binding.durationEt.setText(item.getDuration());
        holder.binding.instructionEt.setText(item.getInstruction());

        holder.binding.medicineNameEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { item.setMedicineName(s.toString()); }
        });
        holder.binding.dosageEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { item.setDosage(s.toString()); }
        });
        holder.binding.frequencyEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { item.setFrequency(s.toString()); }
        });
        holder.binding.durationEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { item.setDuration(s.toString()); }
        });
        holder.binding.instructionEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { item.setInstruction(s.toString()); }
        });

        holder.binding.removeBtn.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, items.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderMedicineRowBinding binding;

        public ViewHolder(ViewholderMedicineRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
