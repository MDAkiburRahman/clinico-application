package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinico.databinding.ViewholderAdminTipBinding;

import java.util.List;
import java.util.Map;

public class AdminTipAdapter extends RecyclerView.Adapter<AdminTipAdapter.ViewHolder> {
    private final List<String> tipKeys;
    private final Map<String, String> tipMap;
    private final OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(String key);
    }

    public AdminTipAdapter(List<String> tipKeys, Map<String, String> tipMap, OnDeleteClickListener listener) {
        this.tipKeys = tipKeys;
        this.tipMap = tipMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAdminTipBinding binding = ViewholderAdminTipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String key = tipKeys.get(position);
        holder.binding.tipTxt.setText(tipMap.get(key));
        holder.binding.deleteBtn.setOnClickListener(v -> listener.onDelete(key));
    }

    @Override
    public int getItemCount() {
        return tipKeys.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderAdminTipBinding binding;
        public ViewHolder(ViewholderAdminTipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
