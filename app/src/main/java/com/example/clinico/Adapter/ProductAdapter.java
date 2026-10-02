package com.example.clinico.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Activity.ProductDetailActivity;
import com.example.clinico.Domain.ProductModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderProductBinding;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final List<ProductModel> items;

    public ProductAdapter(List<ProductModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderProductBinding binding = ViewholderProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductModel item = items.get(position);
        holder.binding.productNameTxt.setText(item.getName());
        holder.binding.categoryTxt.setText(item.getCategory());
        holder.binding.priceTxt.setText("৳" + item.getPrice());

        if (item.getStock() > 0) {
            holder.binding.stockTxt.setText("In Stock");
            holder.binding.stockTxt.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.binding.stockTxt.setText("Out of Stock");
            holder.binding.stockTxt.setTextColor(Color.RED);
        }

        Glide.with(holder.itemView.getContext())
                .load(item.getMainImage())
                .placeholder(R.drawable.empty_box)
                .into(holder.binding.productImg);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("product", item);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.binding.addToCartBtn.setOnClickListener(v -> {
            // Implement cart logic
            if (item.getStock() > 0) {
                // Toast.makeText(holder.itemView.getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
            } else {
                // Toast.makeText(holder.itemView.getContext(), "Out of stock", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderProductBinding binding;
        public ViewHolder(ViewholderProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
