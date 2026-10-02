package com.example.clinico.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderAdminDoctorBinding;

import java.util.List;

public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.ViewHolder> {
    private final List<DoctorsModel> items;
    private final OnDoctorActionListener listener;

    public interface OnDoctorActionListener {
        void onEdit(DoctorsModel doctor);
        void onDelete(DoctorsModel doctor);
        void onFeaturedToggle(DoctorsModel doctor, boolean isFeatured);
        void onManageAvailability(DoctorsModel doctor);
    }

    public AdminDoctorAdapter(List<DoctorsModel> items, OnDoctorActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAdminDoctorBinding binding = ViewholderAdminDoctorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorsModel item = items.get(position);
        holder.binding.nameTxt.setText(item.getName());
        holder.binding.specialTxt.setText(item.getSpecial());
        holder.binding.priceTxt.setText("$" + item.getPrice());
        
        holder.binding.featuredCb.setOnCheckedChangeListener(null); // Clear first
        holder.binding.featuredCb.setChecked(item.isFeatured());
        
        Glide.with(holder.itemView.getContext())
                .load(item.getPicture())
                .placeholder(R.drawable.ic_person)
                .into(holder.binding.img);

        holder.binding.featuredCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onFeaturedToggle(item, isChecked);
        });

        holder.binding.optionsBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), v);
            popup.getMenu().add("Edit Info");
            popup.getMenu().add("Manage Availability");
            popup.getMenu().add("Remove Doctor");
            
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getTitle().equals("Edit Info")) {
                    listener.onEdit(item);
                } else if (menuItem.getTitle().equals("Manage Availability")) {
                    listener.onManageAvailability(item);
                } else if (menuItem.getTitle().equals("Remove Doctor")) {
                    listener.onDelete(item);
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderAdminDoctorBinding binding;
        public ViewHolder(ViewholderAdminDoctorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
