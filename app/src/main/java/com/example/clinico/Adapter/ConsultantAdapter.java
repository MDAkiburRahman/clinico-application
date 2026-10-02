package com.example.clinico.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clinico.Domain.ConsultantModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderConsultantBinding;
import java.util.List;

public class ConsultantAdapter extends RecyclerView.Adapter<ConsultantAdapter.ViewHolder> {
    private List<ConsultantModel> items;

    public ConsultantAdapter(List<ConsultantModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderConsultantBinding binding = ViewholderConsultantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConsultantModel item = items.get(position);
        holder.binding.nameTxt.setText(item.getName());
        holder.binding.specialTxt.setText(item.getSpecial());
        
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(holder.itemView.getContext(), com.example.clinico.Activity.ChatActivity.class);
            intent.putExtra("receiverId", item.getId());
            intent.putExtra("receiverName", item.getName());
            holder.itemView.getContext().startActivity(intent);
        });

        Glide.with(holder.itemView.getContext())
                .load(item.getPicture())
                .placeholder(R.drawable.ic_person)
                .into(holder.binding.img);

        if (item.isOnline()) {
            holder.binding.statusTxt.setText("Online");
            holder.binding.statusTxt.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.premium_success));
            holder.binding.statusIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getResources().getColor(R.color.premium_success)));
            holder.binding.callBtn.setEnabled(true);
            holder.binding.callBtn.setAlpha(1.0f);
        } else {
            holder.binding.statusTxt.setText("Offline");
            holder.binding.statusTxt.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
            holder.binding.statusIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getResources().getColor(R.color.red)));
            holder.binding.callBtn.setEnabled(false);
            holder.binding.callBtn.setAlpha(0.5f);
        }

        holder.binding.callBtn.setOnClickListener(v -> {
            if (item.getPhone() != null && !item.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + item.getPhone()));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderConsultantBinding binding;
        public ViewHolder(ViewholderConsultantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
