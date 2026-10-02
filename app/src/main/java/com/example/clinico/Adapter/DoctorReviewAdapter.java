package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.ReviewModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderReviewItemBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorReviewAdapter extends RecyclerView.Adapter<DoctorReviewAdapter.ViewHolder> {

    private List<ReviewModel> items;

    public DoctorReviewAdapter(List<ReviewModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderReviewItemBinding binding = ViewholderReviewItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel item = items.get(position);
        holder.binding.userNameTxt.setText(item.getUserName());
        holder.binding.ratingBar.setRating(item.getRating());
        holder.binding.commentTxt.setText(item.getComment());
        holder.binding.dateTxt.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(item.getTimestamp())));

        Glide.with(holder.itemView.getContext())
                .load(item.getUserImage())
                .placeholder(R.drawable.profile)
                .into(holder.binding.userImg);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderReviewItemBinding binding;

        public ViewHolder(ViewholderReviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
