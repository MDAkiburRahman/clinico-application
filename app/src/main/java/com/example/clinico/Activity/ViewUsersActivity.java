package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.UserAdapter;
import com.example.clinico.Domain.UserModel;
import com.example.clinico.databinding.ActivityViewUsersBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewUsersActivity extends BaseActivity {
    private ActivityViewUsersBinding binding;
    private List<UserModel> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadUsers();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.userRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList);
        binding.userRv.setAdapter(adapter);
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        UserModel user = data.getValue(UserModel.class);
                        if (user != null) {
                            user.setUid(data.getKey());
                            userList.add(user);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                
                if (userList.isEmpty()) {
                    Toast.makeText(ViewUsersActivity.this, "No users found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ViewUsersActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
