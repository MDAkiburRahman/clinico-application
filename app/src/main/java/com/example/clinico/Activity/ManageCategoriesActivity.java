package com.example.clinico.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AdminCategoryAdapter;
import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.databinding.ActivityManageCategoriesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends BaseActivity {
    private ActivityManageCategoriesBinding binding;
    private List<CategoryModel> catList = new ArrayList<>();
    private AdminCategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadCategories();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.addCatFab.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCategoryActivity.class));
        });

        binding.catRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(catList, new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(CategoryModel category) {
                Intent intent = new Intent(ManageCategoriesActivity.this, AddCategoryActivity.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }

            @Override
            public void onDelete(CategoryModel category) {
                confirmDelete(category);
            }
        });
        binding.catRv.setAdapter(adapter);
    }

    private void loadCategories() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Category").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    CategoryModel model = data.getValue(CategoryModel.class);
                    if (model != null) {
                        model.setKey(data.getKey());
                        catList.add(model);
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

    private void confirmDelete(CategoryModel category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure? Doctors in this category will not be automatically removed.")
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(CategoryModel category) {
        if (category.getKey() == null) return;
        database.getReference("Category").child(category.getKey()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ManageCategoriesActivity.this, "Category removed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
