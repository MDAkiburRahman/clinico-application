package com.example.clinico.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderDateBinding;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private List<String> days;
    private List<String> dates;
    private int selectedPosition = -1;
    private OnDateClickListener listener;
    private Context context;

    public interface OnDateClickListener {
        void onClick(String date);
    }

    public DateAdapter(List<String> days, List<String> dates, OnDateClickListener listener) {
        this.days = days;
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderDateBinding binding = ViewholderDateBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.dayTxt.setText(days.get(position));
        holder.binding.dateTxt.setText(dates.get(position));

        if (selectedPosition == position) {
            holder.binding.dateLayout.setBackgroundResource(R.drawable.blue_btn_bg);
            holder.binding.dayTxt.setTextColor(context.getResources().getColor(R.color.white));
            holder.binding.dateTxt.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.binding.dateLayout.setBackgroundResource(R.drawable.premium_stat_card_bg);
            holder.binding.dayTxt.setTextColor(context.getResources().getColor(R.color.premium_text_gray));
            holder.binding.dateTxt.setTextColor(context.getResources().getColor(R.color.premium_text_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            listener.onClick(dates.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderDateBinding binding;
        public ViewHolder(ViewholderDateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
