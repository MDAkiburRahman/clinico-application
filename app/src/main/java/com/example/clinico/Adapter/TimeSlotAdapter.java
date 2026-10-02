package com.example.clinico.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderTimeBinding;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    private List<String> slots;
    private int selectedPosition = -1;
    private OnTimeClickListener listener;
    private Context context;

    public interface OnTimeClickListener {
        void onClick(String time);
    }

    public TimeSlotAdapter(List<String> slots, OnTimeClickListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderTimeBinding binding = ViewholderTimeBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.timeTxt.setText(slots.get(position));

        if (selectedPosition == position) {
            holder.binding.timeTxt.setBackgroundResource(R.drawable.blue_btn_bg);
            holder.binding.timeTxt.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.binding.timeTxt.setBackgroundResource(R.drawable.premium_stat_card_bg);
            holder.binding.timeTxt.setTextColor(context.getResources().getColor(R.color.premium_text_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            listener.onClick(slots.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderTimeBinding binding;
        public ViewHolder(ViewholderTimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
