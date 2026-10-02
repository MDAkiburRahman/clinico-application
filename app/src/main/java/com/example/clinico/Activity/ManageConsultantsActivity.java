package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clinico.Domain.ConsultantModel;
import com.example.clinico.R;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityManageConsultantsBinding;
import com.example.clinico.databinding.ViewholderAdminConsultantBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ManageConsultantsActivity extends BaseActivity {
    private ActivityManageConsultantsBinding binding;
    private List<ConsultantModel> consultantList = new ArrayList<>();
    private AdminConsultantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageConsultantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadConsultants();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.addConsultantFab.setOnClickListener(v -> startActivity(new Intent(this, AddConsultantActivity.class)));

        binding.consultantRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminConsultantAdapter();
        binding.consultantRv.setAdapter(adapter);
    }

    private void loadConsultants() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference(Constants.NODE_CONSULTANTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                consultantList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ConsultantModel model = ds.getValue(ConsultantModel.class);
                    if (model != null) {
                        model.setId(ds.getKey());
                        consultantList.add(model);
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

    private class AdminConsultantAdapter extends RecyclerView.Adapter<AdminConsultantAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewholderAdminConsultantBinding b = ViewholderAdminConsultantBinding.inflate(getLayoutInflater(), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ConsultantModel item = consultantList.get(position);
            holder.binding.nameTxt.setText(item.getName());
            holder.binding.onlineSwitch.setChecked(item.isOnline());

            Glide.with(ManageConsultantsActivity.this).load(item.getPicture()).placeholder(R.drawable.ic_person).into(holder.binding.img);

            holder.binding.onlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                database.getReference(Constants.NODE_CONSULTANTS).child(item.getId()).child("online").setValue(isChecked);
            });

            holder.binding.editBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ManageConsultantsActivity.this, AddConsultantActivity.class);
                intent.putExtra("consultant", item);
                startActivity(intent);
            });

            holder.binding.deleteBtn.setOnClickListener(v -> {
                database.getReference(Constants.NODE_CONSULTANTS).child(item.getId()).removeValue();
            });
        }

        @Override
        public int getItemCount() { return consultantList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewholderAdminConsultantBinding binding;
            ViewHolder(ViewholderAdminConsultantBinding b) {
                super(b.getRoot());
                binding = b;
            }
        }
    }
}
