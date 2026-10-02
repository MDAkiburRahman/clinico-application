package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.clinico.Adapter.NotificationAdapter;
import com.example.clinico.Domain.NotificationModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.databinding.ActivityNotificationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends BaseActivity {
    private ActivityNotificationBinding binding;
    private List<NotificationModel> notificationList = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadNotifications();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.notificationRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList, notification -> {
            // Mark as read
            if (!notification.isRead()) {
                String uid = FirebaseHelper.getUid();
                notification.setRead(true);
                database.getReference(Constants.NODE_NOTIFICATIONS)
                        .child(uid)
                        .child(notification.getNotificationId())
                        .child("read")
                        .setValue(true);
            }

            // Handle navigation if appointmentId exists
            if (notification.getAppointmentId() != null && !notification.getAppointmentId().isEmpty()) {
                // Navigate to appointment detail or list
                // For now just show toast or go to main appointments
                startActivity(new Intent(this, MyAppointmentsActivity.class));
            }
        });
        binding.notificationRv.setAdapter(adapter);
    }

    private void loadNotifications() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference(Constants.NODE_NOTIFICATIONS).child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            NotificationModel model = ds.getValue(NotificationModel.class);
                            if (model != null) {
                                model.setNotificationId(ds.getKey());
                                notificationList.add(model);
                            }
                        }
                        
                        // Sort by created at descending
                        Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));
                        
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyTxt.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
