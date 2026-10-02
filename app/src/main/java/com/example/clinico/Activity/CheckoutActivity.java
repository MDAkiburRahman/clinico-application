package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.clinico.Domain.CartItemModel;
import com.example.clinico.Domain.OrderModel;
import com.example.clinico.Domain.UserModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityCheckoutBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends BaseActivity {
    private ActivityCheckoutBinding binding;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private List<CartItemModel> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        subtotal = getIntent().getDoubleExtra("subtotal", 0);
        deliveryFee = getIntent().getDoubleExtra("deliveryFee", 0);
        total = subtotal + deliveryFee;

        initView();
        loadUserInfo();
        loadCartItems();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.totalTxt.setText("৳" + total);

        binding.deliveryMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.methodPickup.getId()) {
                binding.addressContainer.setVisibility(View.GONE);
                total = subtotal;
            } else {
                binding.addressContainer.setVisibility(View.VISIBLE);
                total = subtotal + deliveryFee;
            }
            binding.totalTxt.setText("৳" + total);
        });

        binding.placeOrderBtn.setOnClickListener(v -> placeOrder());
    }

    private void loadUserInfo() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    binding.fullNameEt.setText(user.getName());
                    binding.phoneEt.setText(user.getPhone());
                    binding.addressEt.setText(user.getAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCartItems() {
        String uid = FirebaseHelper.getUid();
        database.getReference(Constants.NODE_MEDICINE_CARTS).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartItems.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            CartItemModel item = ds.getValue(CartItemModel.class);
                            if (item != null) cartItems.add(item);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void placeOrder() {
        String fullName = binding.fullNameEt.getText().toString().trim();
        String phone = binding.phoneEt.getText().toString().trim();
        String address = binding.addressEt.getText().toString().trim();
        boolean isHomeDelivery = binding.methodHome.isChecked();

        if (isHomeDelivery && (fullName.isEmpty() || phone.isEmpty() || address.isEmpty())) {
            Toast.makeText(this, "Please fill all address fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.placeOrderBtn.setEnabled(false);

        // Security: Re-fetch prices from database to prevent client-side tampering
        recalculateAndPlaceOrder(fullName, phone, address, isHomeDelivery);
    }

    private void recalculateAndPlaceOrder(String fullName, String phone, String address, boolean isHomeDelivery) {
        database.getReference(Constants.NODE_MEDICAL_PRODUCTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double secureSubtotal = 0;
                for (CartItemModel item : cartItems) {
                    DataSnapshot productSnap = snapshot.child(item.getProductId());
                    if (productSnap.exists()) {
                        Double dbPrice = productSnap.child("price").getValue(Double.class);
                        if (dbPrice != null) {
                            item.setUnitPrice(dbPrice);
                            secureSubtotal += (dbPrice * item.getQuantity());
                        }
                    }
                }

                double secureTotal = secureSubtotal + (isHomeDelivery ? deliveryFee : 0);
                finalizeOrder(fullName, phone, address, isHomeDelivery, secureSubtotal, secureTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.placeOrderBtn.setEnabled(true);
            }
        });
    }

    private void finalizeOrder(String fullName, String phone, String address, boolean isHomeDelivery, double secureSubtotal, double secureTotal) {
        String uid = FirebaseHelper.getUid();
        String orderId = database.getReference(Constants.NODE_MEDICINE_ORDERS).push().getKey();

        String paymentMethod = "bKash";
        if (binding.payNagad.isChecked()) paymentMethod = "Nagad";
        else if (binding.payCod.isChecked()) paymentMethod = "Cash on Delivery";

        OrderModel order = new OrderModel();
        order.setOrderId(orderId);
        order.setUserId(uid);
        order.setItems(cartItems);
        order.setSubtotal(secureSubtotal);
        order.setDeliveryFee(isHomeDelivery ? deliveryFee : 0);
        order.setTotal(secureTotal);
        order.setDeliveryMethod(isHomeDelivery ? "Home Delivery" : "Pick Up");
        order.setFullName(fullName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("Unpaid");
        order.setOrderStatus("Pending");
        order.setCreatedAt(System.currentTimeMillis());

        database.getReference(Constants.NODE_MEDICINE_ORDERS).child(orderId).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    // Clear Cart
                    database.getReference(Constants.NODE_MEDICINE_CARTS).child(uid).removeValue();
                    
                    // Update Stock
                    updateStock();

                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Order placed successfully", Toast.LENGTH_LONG).show();
                    
                    Intent intent = new Intent(this, MedicalCenterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.placeOrderBtn.setEnabled(true);
                    Toast.makeText(this, "Order failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStock() {
        for (CartItemModel item : cartItems) {
            database.getReference(Constants.NODE_MEDICAL_PRODUCTS).child(item.getProductId())
                    .child("stock").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer currentStock = snapshot.getValue(Integer.class);
                    if (currentStock != null) {
                        int newStock = currentStock - item.getQuantity();
                        if (newStock < 0) newStock = 0;
                        database.getReference(Constants.NODE_MEDICAL_PRODUCTS).child(item.getProductId())
                                .child("stock").setValue(newStock);
                        
                        if (newStock == 0) {
                            database.getReference(Constants.NODE_MEDICAL_PRODUCTS).child(item.getProductId())
                                    .child("available").setValue(false);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}
