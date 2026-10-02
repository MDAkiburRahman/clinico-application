package com.example.clinico.Adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.ReviewModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderReviewBinding;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final List<ReviewModel> items;

    public ReviewAdapter(List<ReviewModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderReviewBinding binding = ViewholderReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel item = items.get(position);
        holder.binding.userNameTxt.setText(item.getUserName());
        holder.binding.commentTxt.setText(item.getComment());
        holder.binding.ratingBar.setRating(item.getRating());

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(item.getTimestamp());
        String date = DateFormat.format("dd MMM yyyy", cal).toString();
        holder.binding.dateTxt.setText(date);

        Glide.with(holder.itemView.getContext())
                .load(item.getUserImage())
                .placeholder(R.drawable.ic_person)
                .into(holder.binding.userImg);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderReviewBinding binding;

        public ViewHolder(ViewholderReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
