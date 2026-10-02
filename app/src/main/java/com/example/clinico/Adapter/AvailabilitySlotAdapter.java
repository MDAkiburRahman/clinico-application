package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.databinding.ViewholderAvailabilitySlotBinding;

import java.util.List;

public class AvailabilitySlotAdapter extends RecyclerView.Adapter<AvailabilitySlotAdapter.ViewHolder> {

    private List<DoctorsModel.TimeSlot> items;
    private OnSlotActionListener listener;

    public interface OnSlotActionListener {
        void onRemove(int position);
    }

    public AvailabilitySlotAdapter(List<DoctorsModel.TimeSlot> items, OnSlotActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAvailabilitySlotBinding binding = ViewholderAvailabilitySlotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorsModel.TimeSlot item = items.get(position);
        holder.binding.timeTxt.setText(item.getTime());
        holder.binding.removeBtn.setOnClickListener(v -> listener.onRemove(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderAvailabilitySlotBinding binding;

        public ViewHolder(ViewholderAvailabilitySlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
