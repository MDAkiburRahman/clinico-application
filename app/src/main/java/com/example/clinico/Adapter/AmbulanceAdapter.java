package com.example.clinico.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.Domain.EmergencyContactModel;
import com.example.clinico.databinding.ViewholderAmbulanceBinding;
import java.util.List;

public class AmbulanceAdapter extends RecyclerView.Adapter<AmbulanceAdapter.ViewHolder> {
    private List<EmergencyContactModel> items;
    private boolean isAdmin;
    private OnAmbulanceActionListener listener;

    public interface OnAmbulanceActionListener {
        void onDelete(EmergencyContactModel item);
        void onEdit(EmergencyContactModel item);
    }

    public AmbulanceAdapter(List<EmergencyContactModel> items, boolean isAdmin, OnAmbulanceActionListener listener) {
        this.items = items;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAmbulanceBinding binding = ViewholderAmbulanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContactModel item = items.get(position);
        holder.binding.nameTxt.setText(item.getName());
        holder.binding.phoneTxt.setText(item.getPhone());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + item.getPhone()));
            holder.itemView.getContext().startActivity(intent);
        });

        if (isAdmin) {
            holder.binding.adminOptions.setVisibility(View.VISIBLE);
            holder.binding.deleteBtn.setOnClickListener(v -> listener.onDelete(item));
            holder.binding.editBtn.setOnClickListener(v -> listener.onEdit(item));
        } else {
            holder.binding.adminOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderAmbulanceBinding binding;

        public ViewHolder(ViewholderAmbulanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
