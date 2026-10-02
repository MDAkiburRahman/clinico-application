package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.ProductAdapter;
import com.example.clinico.Domain.ProductModel;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityManageProductsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends BaseActivity {
    private ActivityManageProductsBinding binding;
    private List<ProductModel> productList = new ArrayList<>();
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadProducts();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.addProductFab.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));

        binding.productRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList); // Reusing but I should probably make an admin version
        binding.productRv.setAdapter(adapter);
    }

    private void loadProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference(Constants.NODE_MEDICAL_PRODUCTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ProductModel product = ds.getValue(ProductModel.class);
                            if (product != null) {
                                product.setProductId(ds.getKey());
                                productList.add(product);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void deleteProduct(ProductModel product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.getReference(Constants.NODE_MEDICAL_PRODUCTS).child(product.getProductId()).removeValue();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
