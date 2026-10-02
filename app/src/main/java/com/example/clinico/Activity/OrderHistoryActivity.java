package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.ManageOrderAdapter;
import com.example.clinico.Domain.OrderModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityOrderHistoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistoryActivity extends BaseActivity {
    private ActivityOrderHistoryBinding binding;
    private List<OrderModel> orderList = new ArrayList<>();
    private ManageOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadOrders();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.orderRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageOrderAdapter(orderList, null); // Passing null listener hides update button
        binding.orderRv.setAdapter(adapter);
    }

    private void loadOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference(Constants.NODE_MEDICINE_ORDERS)
                .orderByChild("userId")
                .equalTo(uid)
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
                        Collections.reverse(orderList); // Show newest first
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyTxt.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
