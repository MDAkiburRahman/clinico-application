package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderAdminCategoryBinding;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {
    private final List<CategoryModel> items;
    private final OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEdit(CategoryModel category);
        void onDelete(CategoryModel category);
    }

    public AdminCategoryAdapter(List<CategoryModel> items, OnCategoryActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAdminCategoryBinding binding = ViewholderAdminCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel item = items.get(position);
        holder.binding.nameTxt.setText(item.getName());
        
        Glide.with(holder.itemView.getContext())
                .load(item.getPicture())
                .into(holder.binding.img);

        holder.binding.editBtn.setOnClickListener(v -> listener.onEdit(item));
        holder.binding.deleteBtn.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderAdminCategoryBinding binding;
        public ViewHolder(ViewholderAdminCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
