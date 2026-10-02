package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.CategoryAdapter;
import com.example.clinico.Adapter.ProductAdapter;
import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.Domain.ProductModel;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityMedicalCenterBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MedicalCenterActivity extends BaseActivity {
    private ActivityMedicalCenterBinding binding;
    private List<ProductModel> productList = new ArrayList<>();
    private List<ProductModel> filteredList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicalCenterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadCategories();
        loadProducts();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.cartBtn.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        binding.productRv.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(filteredList);
        binding.productRv.setAdapter(productAdapter);

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadCategories() {
        List<CategoryModel> categories = new ArrayList<>();
        categories.add(new CategoryModel(0, "All", ""));
        categories.add(new CategoryModel(1, "Pain Relief", ""));
        categories.add(new CategoryModel(2, "Fever & Cold", ""));
        categories.add(new CategoryModel(3, "Vitamins", ""));
        categories.add(new CategoryModel(4, "Diabetes Care", ""));
        categories.add(new CategoryModel(5, "Blood Pressure", ""));
        categories.add(new CategoryModel(6, "First Aid", ""));
        categories.add(new CategoryModel(7, "Personal Care", ""));

        binding.categoryRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRv.setAdapter(new CategoryAdapter(categories, category -> {
            selectedCategory = category.getName();
            filterProducts(binding.searchEt.getText().toString());
            binding.productTitle.setText(selectedCategory.equals("All") ? "All Products" : selectedCategory);
        }));
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
                        filterProducts(binding.searchEt.getText().toString());
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        for (ProductModel p : productList) {
            boolean matchesCategory = selectedCategory.equals("All") || p.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesQuery = p.getName().toLowerCase().contains(query.toLowerCase()) || 
                                 (p.getGenericName() != null && p.getGenericName().toLowerCase().contains(query.toLowerCase()));
            
            if (matchesCategory && matchesQuery) {
                filteredList.add(p);
            }
        }
        productAdapter.notifyDataSetChanged();
        binding.emptyTxt.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
