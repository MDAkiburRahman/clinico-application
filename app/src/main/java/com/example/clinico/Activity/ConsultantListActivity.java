package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.clinico.Adapter.ConsultantAdapter;
import com.example.clinico.Domain.ConsultantModel;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityConsultantListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ConsultantListActivity extends BaseActivity {
    private ActivityConsultantListBinding binding;
    private List<ConsultantModel> consultantList = new ArrayList<>();
    private ConsultantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsultantListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadConsultants();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.consultantRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConsultantAdapter(consultantList);
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
                binding.emptyTxt.setVisibility(consultantList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}
