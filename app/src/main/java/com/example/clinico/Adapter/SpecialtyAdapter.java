package com.example.clinico.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.R;
import com.example.clinico.databinding.ItemSpecialtyFilterBinding;
import java.util.List;

public class SpecialtyAdapter extends RecyclerView.Adapter<SpecialtyAdapter.ViewHolder> {
    private List<String> items;
    private int selectedPosition = 0;
    private OnSpecialtyClickListener listener;
    private Context context;

    public interface OnSpecialtyClickListener {
        void onClick(String specialty);
    }

    public SpecialtyAdapter(List<String> items, OnSpecialtyClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ItemSpecialtyFilterBinding binding = ItemSpecialtyFilterBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position);
        holder.binding.specialtyName.setText(item);

        if (selectedPosition == position) {
            holder.binding.filterContainer.setBackgroundResource(R.drawable.blue_btn_bg);
            holder.binding.specialtyName.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.binding.filterContainer.setBackgroundResource(R.drawable.premium_stat_card_bg);
            holder.binding.specialtyName.setTextColor(context.getResources().getColor(R.color.premium_text_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemSpecialtyFilterBinding binding;
        public ViewHolder(ItemSpecialtyFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
