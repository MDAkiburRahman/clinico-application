package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.clinico.databinding.ActivityAdminDashboardBinding;

public class AdminDashboardActivity extends BaseActivity {
    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.manageDoctorsBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageDoctorsActivity.class));
        });

        binding.manageCategoriesBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageCategoriesActivity.class));
        });

        binding.manageAppointmentsBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminAppointmentsActivity.class));
        });

        binding.viewUsersBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ViewUsersActivity.class));
        });

        binding.manageBannersBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageBannersActivity.class));
        });

        binding.manageTipsBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageTipsActivity.class));
        });

        binding.globalConfigBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, GlobalConfigActivity.class));
        });

        binding.manageConsultantsBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageConsultantsActivity.class));
        });

        binding.manageMedicineBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageProductsActivity.class));
        });

        binding.medicineOrdersBtn.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageOrdersActivity.class));
        });
    }
}
