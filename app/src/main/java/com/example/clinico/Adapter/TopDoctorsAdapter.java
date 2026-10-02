package com.example.clinico.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.clinico.Activity.DetailActivity;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.databinding.ViewholderTopDoctorBinding;

import java.util.List;

public class TopDoctorsAdapter extends RecyclerView.Adapter<TopDoctorsAdapter.ViewHolder> {

    private final List<DoctorsModel> items;
    private Context context;

    public TopDoctorsAdapter(List<DoctorsModel> items) {
        this.items = items;
    }


    @NonNull
    @Override
    public TopDoctorsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderTopDoctorBinding binding = ViewholderTopDoctorBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TopDoctorsAdapter.ViewHolder holder, int position) {
        DoctorsModel doctorsModel = items.get(position);
        holder.binding.nameTxt.setText(doctorsModel.getName());
        holder.binding.specialTxt.setText(doctorsModel.getSpecial());
        holder.binding.ratingTxt.setText(doctorsModel.getRating()+ "");
        holder.binding.patiensTxt.setText(doctorsModel.getPatiens()+ " Patients");


        Glide.with(holder.itemView.getContext())
                .load(doctorsModel.getPicture())
                .apply(new RequestOptions()
                        .transform(new CenterCrop())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(300, 400))
                .into(holder.binding.img);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", doctorsModel);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderTopDoctorBinding binding;

        public ViewHolder(ViewholderTopDoctorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
