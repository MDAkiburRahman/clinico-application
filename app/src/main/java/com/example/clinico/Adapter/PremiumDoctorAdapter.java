package com.example.clinico.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clinico.Activity.DetailActivity;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderDoctorPremiumBinding;
import java.util.List;

public class PremiumDoctorAdapter extends RecyclerView.Adapter<PremiumDoctorAdapter.ViewHolder> {
    private List<DoctorsModel> items;
    private Context context;

    public PremiumDoctorAdapter(List<DoctorsModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderDoctorPremiumBinding binding = ViewholderDoctorPremiumBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorsModel item = items.get(position);
        holder.binding.doctorNameTxt.setText(item.getName());
        holder.binding.specializationTxt.setText(item.getSpecial());
        holder.binding.ratingTxt.setText(String.valueOf(item.getRating()));
        holder.binding.reviewTxt.setText("· " + item.getReviewCount() + " Reviews");
        holder.binding.experienceTxt.setText(item.getExperience() + "+ Years");
        holder.binding.feeTxt.setText("৳" + item.getPrice());

        Glide.with(context)
                .load(item.getPicture())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.binding.doctorImg);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });

        holder.binding.viewProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderDoctorPremiumBinding binding;
        public ViewHolder(ViewholderDoctorPremiumBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
