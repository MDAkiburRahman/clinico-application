package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.BannerModel;
import com.example.clinico.R;

import java.util.List;

public class AdminBannerAdapter extends RecyclerView.Adapter<AdminBannerAdapter.ViewHolder> {

    private final List<BannerModel> items;
    private final OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(BannerModel banner, int position);
    }

    public AdminBannerAdapter(List<BannerModel> items, OnDeleteClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_admin_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BannerModel banner = items.get(position);
        
        Glide.with(holder.itemView.getContext())
                .load(banner.getUrl())
                .into(holder.bannerImg);
        
        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(banner, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImg, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImg = itemView.findViewById(R.id.bannerImg);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
