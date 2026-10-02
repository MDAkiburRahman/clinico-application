package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.ManageOrderAdapter;
import com.example.clinico.Domain.OrderModel;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityManageOrdersBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends BaseActivity {
    private ActivityManageOrdersBinding binding;
    private List<OrderModel> orderList = new ArrayList<>();
    private List<OrderModel> filteredList = new ArrayList<>();
    private ManageOrderAdapter adapter;
    private String selectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadOrders();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.orderRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageOrderAdapter(filteredList, this::updateOrderStatus);
        binding.orderRv.setAdapter(adapter);

        binding.statusChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                selectedStatus = chip.getText().toString();
                filterOrders();
            }
        });
    }

    private void loadOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference(Constants.NODE_MEDICINE_ORDERS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            OrderModel order = ds.getValue(OrderModel.class);
                            if (order != null) {
                                order.setOrderId(ds.getKey());
                                orderList.add(order);
                            }
                        }
                        filterOrders();
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void filterOrders() {
        filteredList.clear();
        for (OrderModel order : orderList) {
            if (selectedStatus.equals("All") || order.getOrderStatus().equals(selectedStatus)) {
                filteredList.add(order);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateOrderStatus(OrderModel order, String newStatus) {
        database.getReference(Constants.NODE_MEDICINE_ORDERS)
                .child(order.getOrderId())
                .child("orderStatus")
                .setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Send Notification to user
                    String notificationId = database.getReference("Notifications").child(order.getUserId()).push().getKey();
                    if (notificationId != null) {
                        java.util.Map<String, Object> notif = new java.util.HashMap<>();
                        notif.put("orderId", order.getOrderId());
                        notif.put("title", "Order Updated");
                        notif.put("message", "Your medicine order #" + order.getOrderId().substring(Math.max(0, order.getOrderId().length() - 8)) + " is now " + newStatus);
                        notif.put("timestamp", System.currentTimeMillis());
                        notif.put("status", "new");
                        
                        database.getReference("Notifications").child(order.getUserId()).child(notificationId).setValue(notif);
                    }
                });
    }
}
