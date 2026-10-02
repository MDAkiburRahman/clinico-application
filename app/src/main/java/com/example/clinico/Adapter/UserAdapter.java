package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.UserModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderUserBinding;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final List<UserModel> items;

    public UserAdapter(List<UserModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderUserBinding binding = ViewholderUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = items.get(position);
        holder.binding.userNameTxt.setText(user.getName());
        holder.binding.userEmailTxt.setText(user.getEmail());
        
        if (user.isAdmin()) {
            holder.binding.adminTag.setVisibility(View.VISIBLE);
        } else {
            holder.binding.adminTag.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePic())
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(holder.binding.userImg);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderUserBinding binding;
        public ViewHolder(ViewholderUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
