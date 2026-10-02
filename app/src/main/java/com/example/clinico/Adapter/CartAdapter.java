package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clinico.Domain.CartItemModel;
import com.example.clinico.Helper.CartHelper;
import com.example.clinico.R;
import com.example.clinico.databinding.ViewholderCartBinding;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final List<CartItemModel> items;
    private final CartChangeListener changeListener;

    public interface CartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(List<CartItemModel> items, CartChangeListener changeListener) {
        this.items = items;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemModel item = items.get(position);
        holder.binding.nameTxt.setText(item.getProductName());
        holder.binding.priceTxt.setText("৳" + item.getUnitPrice());
        holder.binding.qtyTxt.setText(String.valueOf(item.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(item.getProductImage())
                .placeholder(R.drawable.empty_box)
                .into(holder.binding.productImg);

        holder.binding.plusBtn.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            updateCartItem(item);
        });

        holder.binding.minusBtn.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                updateCartItem(item);
            }
        });

        holder.binding.removeBtn.setOnClickListener(v -> {
            CartHelper.removeFromCart(item.getProductId(), new CartHelper.CartActionListener() {
                @Override
                public void onSuccess() {
                    changeListener.onCartChanged();
                }

                @Override
                public void onError(String error) {}
            });
        });
    }

    private void updateCartItem(CartItemModel item) {
        com.example.clinico.Helper.FirebaseHelper.getDatabase()
                .getReference(com.example.clinico.Utils.Constants.NODE_MEDICINE_CARTS)
                .child(com.example.clinico.Helper.FirebaseHelper.getUid())
                .child(item.getProductId())
                .setValue(item)
                .addOnSuccessListener(aVoid -> changeListener.onCartChanged());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;
        public ViewHolder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
