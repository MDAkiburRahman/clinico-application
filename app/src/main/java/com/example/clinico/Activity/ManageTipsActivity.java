package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.AdminTipAdapter;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityManageTipsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageTipsActivity extends BaseActivity {
    private ActivityManageTipsBinding binding;
    private List<String> tipKeys = new ArrayList<>();
    private Map<String, String> tipMap = new HashMap<>();
    private AdminTipAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadTips();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.addBtn.setOnClickListener(v -> {
            String tip = binding.tipEt.getText().toString();
            if (!tip.isEmpty()) {
                addTip(tip);
            }
        });

        binding.tipsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminTipAdapter(tipKeys, tipMap, this::deleteTip);
        binding.tipsRv.setAdapter(adapter);
    }

    private void loadTips() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference(Constants.NODE_HEALTH_TIPS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tipKeys.clear();
                tipMap.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String key = data.getKey();
                    String tip = data.getValue(String.class);
                    if (key != null && tip != null) {
                        tipKeys.add(key);
                        tipMap.put(key, tip);
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

    private void addTip(String tip) {
        database.getReference(Constants.NODE_HEALTH_TIPS).push().setValue(tip).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                binding.tipEt.setText("");
                Toast.makeText(this, "Tip Added", Toast.LENGTH_SHORT).show();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Failed to add tip: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteTip(String key) {
        database.getReference(Constants.NODE_HEALTH_TIPS).child(key).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Tip Removed", Toast.LENGTH_SHORT).show();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Failed to remove: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
