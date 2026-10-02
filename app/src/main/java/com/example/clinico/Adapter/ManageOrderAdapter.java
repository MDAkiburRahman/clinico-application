package com.example.clinico.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinico.Domain.CartItemModel;
import com.example.clinico.Domain.OrderModel;
import com.example.clinico.databinding.ViewholderOrderBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageOrderAdapter extends RecyclerView.Adapter<ManageOrderAdapter.ViewHolder> {
    private final List<OrderModel> items;
    private final OrderActionListener listener;

    public interface OrderActionListener {
        void onUpdateStatus(OrderModel order, String newStatus);
    }

    public ManageOrderAdapter(List<OrderModel> items, OrderActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderOrderBinding binding = ViewholderOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel item = items.get(position);
        holder.binding.orderIdTxt.setText("Order #" + item.getOrderId().substring(Math.max(0, item.getOrderId().length() - 8)));
        holder.binding.statusTxt.setText(item.getOrderStatus());
        holder.binding.dateTxt.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date(item.getCreatedAt())));
        holder.binding.totalTxt.setText("Total: ৳" + item.getTotal());
        holder.binding.deliveryMethodTxt.setText(item.getDeliveryMethod());

        StringBuilder itemsStr = new StringBuilder();
        for (CartItemModel cartItem : item.getItems()) {
            itemsStr.append(cartItem.getQuantity()).append(" x ").append(cartItem.getProductName()).append("\n");
        }
        holder.binding.itemsTxt.setText(itemsStr.toString().trim());

        if (listener != null) {
            holder.binding.updateBtn.setVisibility(View.VISIBLE);
            holder.binding.updateBtn.setOnClickListener(v -> {
                String[] statuses = {"Pending", "Confirmed", "Processing", "Out for Delivery", "Delivered", "Cancelled"};
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Update Status")
                        .setItems(statuses, (dialog, which) -> {
                            listener.onUpdateStatus(item, statuses[which]);
                        })
                        .show();
            });
        } else {
            holder.binding.updateBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderOrderBinding binding;
        public ViewHolder(ViewholderOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
