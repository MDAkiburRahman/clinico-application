package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.CartAdapter;
import com.example.clinico.Domain.CartItemModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityCartBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity {
    private ActivityCartBinding binding;
    private List<CartItemModel> cartList = new ArrayList<>();
    private CartAdapter adapter;
    private double subtotal = 0;
    private double deliveryFee = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadCart();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.checkoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("subtotal", subtotal);
            intent.putExtra("deliveryFee", deliveryFee);
            intent.putExtra("total", subtotal + deliveryFee);
            startActivity(intent);
        });

        binding.cartRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartList, this::calculateTotals);
        binding.cartRv.setAdapter(adapter);
    }

    private void loadCart() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference(Constants.NODE_MEDICINE_CARTS).child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            CartItemModel item = ds.getValue(CartItemModel.class);
                            if (item != null) {
                                cartList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        calculateTotals();
                        binding.progressBar.setVisibility(View.GONE);
                        
                        boolean isEmpty = cartList.isEmpty();
                        binding.emptyTxt.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                        binding.summaryCard.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void calculateTotals() {
        subtotal = 0;
        for (CartItemModel item : cartList) {
            subtotal += item.getSubtotal();
        }

        binding.subtotalTxt.setText("৳" + subtotal);
        binding.deliveryTxt.setText("৳" + (subtotal > 0 ? deliveryFee : 0));
        binding.totalTxt.setText("৳" + (subtotal > 0 ? (subtotal + deliveryFee) : 0));
    }
}
