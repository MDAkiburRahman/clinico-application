package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.clinico.Adapter.CategoryAdapter;
import com.example.clinico.Domain.CategoryModel;
import com.example.clinico.databinding.ActivityCategoryListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryListActivity extends BaseActivity {
    private ActivityCategoryListBinding binding;
    private List<CategoryModel> catList = new ArrayList<>();
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadCategories();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.catRv.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CategoryAdapter(catList, category -> {
            Intent intent = new Intent(CategoryListActivity.this, SearchActivity.class);
            intent.putExtra("category", category.getName());
            startActivity(intent);
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
}
