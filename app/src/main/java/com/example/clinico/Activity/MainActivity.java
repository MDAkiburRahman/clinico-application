package com.example.clinico.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.clinico.Adapter.BannerAdapter;
import com.example.clinico.Adapter.CategoryAdapter;
import com.example.clinico.Adapter.MedicationAdapter;
import com.example.clinico.Adapter.SpecialtyAdapter;
import com.example.clinico.Adapter.TopDoctorsAdapter;
import com.example.clinico.Adapter.UpcomingAppointmentAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.Domain.MedicationModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.R;
import com.example.clinico.ViewModel.MainViewModel;
import com.example.clinico.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private DrawerLayout drawerLayout;
    private boolean isInitialized = false;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private List<String> healthTips = new ArrayList<>();
    private java.util.Random random = new java.util.Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            viewModel = new ViewModelProvider(this).get(MainViewModel.class);

            View mainView = findViewById(R.id.main);
            if (mainView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }

            if (checkInternet()) {
                initApp();
                initDrawer();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initApp() {
        if (binding == null || isInitialized) return;
        isInitialized = true;
        
        checkAndSeedDoctors();
        checkAndSeedMedicines();
        checkAndSeedMedications();
        initHomeSections();
        initUser();
        
        initBanners();
        initCategories();
        initTopDoctors();
        initHealthTips();
        initMedications();
        loadAppLogo(binding.navHeader.headerGraphic);
    }

    private void checkAndSeedMedicines() {
        database.getReference(com.example.clinico.Utils.Constants.NODE_MEDICAL_PRODUCTS).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    com.example.clinico.Helper.SampleDataHelper.seedMedicines();
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void checkAndSeedDoctors() {
        database.getReference("Doctors").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    com.example.clinico.Helper.SampleDataHelper.seedDoctors();
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void initHomeSections() {
        // Load REAL Upcoming Appointments for the patient
        String uid = FirebaseHelper.getUid();
        if (uid != null) {
            database.getReference("Appointments")
                    .orderByChild("userId")
                    .equalTo(uid)
                    .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    List<AppointmentModel> appointments = new ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                        AppointmentModel model = ds.getValue(AppointmentModel.class);
                        if (model != null && ("Pending".equals(model.getStatus()) || "Confirmed".equals(model.getStatus()))) {
                            appointments.add(model);
                        }
                    }
                    binding.appointmentsRv.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                    binding.appointmentsRv.setAdapter(new UpcomingAppointmentAdapter(appointments));
                }

                @Override
                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
            });
        }

        // 5. Quick Actions
        setupActionCard(binding.actionBook, "Book Appointment", R.drawable.bottom_btn2);
        setupActionCard(binding.actionFind, "Medical Center", R.drawable.ic_medical_plus);
        setupActionCard(binding.actionRecords, "Medical Records", R.drawable.ic_health_records);
        setupActionCard(binding.actionPrescriptions, "Prescriptions", R.drawable.ic_menu);

        // 6. Bottom Navigation Listeners
        binding.navHome.setOnClickListener(v -> {}); // Already on Home
        binding.navAppointments.setOnClickListener(v -> startActivity(new Intent(this, MyAppointmentsActivity.class)));
        binding.navHealth.setOnClickListener(v -> startActivity(new Intent(this, HealthActivity.class)));
        binding.navMeds.setOnClickListener(v -> startActivity(new Intent(this, MedicationActivity.class)));
        
        binding.viewAllAppointments.setOnClickListener(v -> startActivity(new Intent(this, MyAppointmentsActivity.class)));

        // Emergency Button (Point 17)
        binding.emergencyBtn.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));

        // 24/7 Consultant
        binding.consultNowBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ConsultantListActivity.class));
        });

        // Search Bar
        binding.editTextText.setFocusable(false);
        binding.editTextText.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        // See All Categories
        binding.seeAllCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoryListActivity.class)));
    }

    private void setupActionCard(com.example.clinico.databinding.ItemQuickActionBinding b, String label, int icon) {
        b.actionLabel.setText(label);
        b.actionIcon.setImageResource(icon);
        b.getRoot().setOnClickListener(v -> {
            if (label.contains("Medical Center")) {
                startActivity(new Intent(this, MedicalCenterActivity.class));
            } else if (label.contains("Book")) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("mode", "book");
                startActivity(intent);
            } else if (label.contains("Records")) {
                startActivity(new Intent(this, MedicalHistoryActivity.class));
            } else if (label.contains("Prescriptions")) {
                startActivity(new Intent(this, PrescriptionActivity.class));
            } else {
                Toast.makeText(this, label + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onInternetRestored() {
        super.onInternetRestored();
        if (!isInitialized) {
            initApp();
        }
    }

    private void initDrawer() {
        drawerLayout = binding.drawerLayout;
        
        if (binding.menuBtn != null) {
            binding.menuBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        // Home
        binding.navHomeItem.setOnClickListener(v -> {
            updateDrawerSelection(R.id.nav_home_item);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Profile
        binding.navHeader.navProfileCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Appointments
        binding.navAppointmentsItem.setOnClickListener(v -> {
            updateDrawerSelection(R.id.nav_appointments_item);
            startActivity(new Intent(this, MyAppointmentsActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Find Doctors
        binding.navFindDoctorsItem.setOnClickListener(v -> {
            updateDrawerSelection(R.id.nav_find_doctors_item);
            startActivity(new Intent(this, SearchActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Health Records
        binding.navHistoryItem.setOnClickListener(v -> {
            updateDrawerSelection(R.id.nav_history_item);
            startActivity(new Intent(this, MedicalHistoryActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Notifications
        binding.navNotificationsItem.setOnClickListener(v -> {
            updateDrawerSelection(R.id.nav_notifications_item);
            startActivity(new Intent(this, NotificationActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Messages
        binding.navMessagesItem.setOnClickListener(v -> {
            updateDrawerSelection(R.id.nav_messages_item);
            startActivity(new Intent(this, MessagesActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Settings
        binding.navSettingsItem.setOnClickListener(v -> {
            updateDrawerSelection(R.id.nav_settings_item);
            startActivity(new Intent(this, SettingsActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Logout
        binding.navLogoutItem.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        binding.notificationLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        initNotificationBadge();
    }

    private void initNotificationBadge() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference(com.example.clinico.Utils.Constants.NODE_NOTIFICATIONS).child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int unreadCount = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            com.example.clinico.Domain.NotificationModel model = ds.getValue(com.example.clinico.Domain.NotificationModel.class);
                            if (model != null && !model.isRead()) {
                                unreadCount++;
                            }
                        }

                        if (unreadCount > 0) {
                            binding.notificationBadge.setVisibility(View.VISIBLE);
                            binding.notificationBadge.setText(String.valueOf(unreadCount));
                        } else {
                            binding.notificationBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateDrawerSelection(int selectedId) {
        resetItem(binding.navHomeItem, binding.navHomeIcon, binding.navHomeText);
        resetItem(binding.navAppointmentsItem, binding.navAppointmentsIcon, binding.navAppointmentsText);
        resetItem(binding.navFindDoctorsItem, binding.navFindDoctorsIcon, binding.navFindDoctorsText);
        resetItem(binding.navHistoryItem, binding.navHistoryIcon, binding.navHistoryText);
        resetItem(binding.navNotificationsItem, binding.navNotificationsIcon, binding.navNotificationsText);
        resetItem(binding.navMessagesItem, binding.navMessagesIcon, binding.navMessagesText);
        resetItem(binding.navSettingsItem, binding.navSettingsIcon, binding.navSettingsText);

        if (selectedId == R.id.nav_home_item) {
            highlightItem(binding.navHomeItem, binding.navHomeIcon, binding.navHomeText);
        } else if (selectedId == R.id.nav_appointments_item) {
            highlightItem(binding.navAppointmentsItem, binding.navAppointmentsIcon, binding.navAppointmentsText);
        } else if (selectedId == R.id.nav_find_doctors_item) {
            highlightItem(binding.navFindDoctorsItem, binding.navFindDoctorsIcon, binding.navFindDoctorsText);
        } else if (selectedId == R.id.nav_history_item) {
            highlightItem(binding.navHistoryItem, binding.navHistoryIcon, binding.navHistoryText);
        } else if (selectedId == R.id.nav_notifications_item) {
            highlightItem(binding.navNotificationsItem, binding.navNotificationsIcon, binding.navNotificationsText);
        } else if (selectedId == R.id.nav_messages_item) {
            highlightItem(binding.navMessagesItem, binding.navMessagesIcon, binding.navMessagesText);
        } else if (selectedId == R.id.nav_settings_item) {
            highlightItem(binding.navSettingsItem, binding.navSettingsIcon, binding.navSettingsText);
        }
    }

    private void resetItem(View container, ImageView icon, TextView text) {
        container.setBackgroundResource(0);
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.icon_default)));
        text.setTextColor(getResources().getColor(R.color.text_primary));
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        container.getLayoutParams().height = (int) (64 * getResources().getDisplayMetrics().density);
        container.requestLayout();
    }

    private void highlightItem(View container, ImageView icon, TextView text) {
        container.setBackgroundResource(R.drawable.nav_item_active_bg);
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_blue)));
        text.setTextColor(getResources().getColor(R.color.primary_blue));
        text.setTypeface(null, android.graphics.Typeface.BOLD);

        container.getLayoutParams().height = (int) (58 * getResources().getDisplayMetrics().density);
        container.requestLayout();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initUser() {
        if (binding == null) return;

        try {
            if (mAuth != null && mAuth.getCurrentUser() != null && database != null) {
                database.getReference("Users").child(mAuth.getCurrentUser().getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    if (snapshot.exists() && binding != null) {
                                        String name = snapshot.child("name").getValue(String.class);
                                        binding.greetingText.setText("Good Morning, " + (name != null ? name : "User") + "!");
                                        updateNavHeader(snapshot);
                                        
                                        String profilePic = snapshot.child("profilePic").getValue(String.class);
                                        if (profilePic != null && !profilePic.isEmpty()) {
                                            Glide.with(MainActivity.this)
                                                    .load(profilePic)
                                                    .placeholder(R.drawable.profile)
                                                    .error(R.drawable.profile)
                                                    .circleCrop()
                                                    .into(binding.userProfileImg);
                                        }
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("MainActivity", "Error in onDataChange", e);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                android.util.Log.e("MainActivity", "Database error: " + error.getMessage());
                            }
                        });
            }

            binding.userProfileImg.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNavHeader(DataSnapshot snapshot) {
        if (binding.navHeader == null) return;
        TextView nameTv = binding.navHeader.navUserName;
        TextView roleTv = binding.navHeader.navUserRole;
        ImageView profileIv = binding.navHeader.navUserImage;

        String name = snapshot.child("name").getValue(String.class);
        String role = snapshot.child("role").getValue(String.class);
        String profilePic = snapshot.child("profilePic").getValue(String.class);

        if (nameTv != null) nameTv.setText(name != null ? name : "User");
        if (roleTv != null) roleTv.setText(role != null ? role : "Patient");

        if (profileIv != null && profilePic != null && !profilePic.isEmpty()) {
            Glide.with(this)
                    .load(profilePic)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(profileIv);
        }
    }

    private void initBanners() {
        if (viewModel == null) return;
        viewModel.loadBanners().observe(this, banners -> {
            if (banners != null && !banners.isEmpty() && binding.bannerViewPager != null) {
                binding.bannerViewPager.setAdapter(new BannerAdapter(banners));
                setupAutoRotation(banners.size());
            }
        });
    }

    private void setupAutoRotation(int size) {
        if (bannerRunnable != null) bannerHandler.removeCallbacks(bannerRunnable);
        
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = binding.bannerViewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % size;
                binding.bannerViewPager.setCurrentItem(nextItem, true);
                bannerHandler.postDelayed(this, 3000); // 3 seconds delay
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bannerRunnable != null) bannerHandler.removeCallbacks(bannerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rotation will be restarted by initBanners if data is observed again, 
        // but since it's a LiveData, we might need to manually trigger or 
        // check if it's already running.
        if (bannerRunnable != null) bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private void initTopDoctors() {
        if (binding.shimmerDoctor != null) {
            binding.shimmerDoctor.startShimmer();
            binding.shimmerDoctor.setVisibility(View.VISIBLE);
        }

        if (viewModel == null) return;
        viewModel.loadDoctors().observe(this, doctorsModels -> {
            if (binding.shimmerDoctor != null) {
                binding.shimmerDoctor.stopShimmer();
                binding.shimmerDoctor.setVisibility(View.GONE);
            }
            
            if (doctorsModels == null) return;

            // Normal Top Doctors
            if (binding.doctorView != null) {
                binding.doctorView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                binding.doctorView.setAdapter(new TopDoctorsAdapter(doctorsModels));
            }

            // Featured Doctors
            List<DoctorsModel> featured = new ArrayList<>();
            for (DoctorsModel doc : doctorsModels) {
                if (doc != null && doc.isFeatured()) {
                    featured.add(doc);
                }
            }
            if (binding.featuredDoctorView != null) {
                binding.featuredDoctorView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                binding.featuredDoctorView.setAdapter(new TopDoctorsAdapter(featured));
            }
        });

        if (binding.seeAllFeatured != null) {
            binding.seeAllFeatured.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("filter", "featured");
                startActivity(intent);
            });
        }

        if (binding.textView5 != null) {
            binding.textView5.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("filter", "top");
                startActivity(intent);
            });
        }
    }

    private void initCategories() {
        if (binding.shimmerCat != null) {
            binding.shimmerCat.startShimmer();
            binding.shimmerCat.setVisibility(View.VISIBLE);
        }

        if (viewModel == null) return;
        viewModel.loadCategories().observe(this, categoryModels -> {
            if (binding.shimmerCat != null) {
                binding.shimmerCat.stopShimmer();
                binding.shimmerCat.setVisibility(View.GONE);
            }
            
            if (categoryModels != null && binding.CatView != null) {
                binding.CatView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                binding.CatView.setAdapter(new CategoryAdapter(categoryModels, category -> {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("category", category.getName());
                    startActivity(intent);
                }));
            }
        });
    }

    private void initMedications() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference("Medications").orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MedicationModel> list = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MedicationModel model = ds.getValue(MedicationModel.class);
                            if (model != null) {
                                model.setId(ds.getKey());
                                if (model.shouldReset()) {
                                    model.setIsTaken(false);
                                    model.setLastTakenTimestamp(0);
                                    database.getReference("Medications").child(model.getId()).child("isTaken").setValue(false);
                                    database.getReference("Medications").child(model.getId()).child("lastTakenTimestamp").setValue(0);
                                }
                                list.add(model);
                            }
                        }
                        
                        // Sort by time
                        java.util.Collections.sort(list, (m1, m2) -> m1.getTime().compareTo(m2.getTime()));

                        binding.medicationRv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        binding.medicationRv.setAdapter(new MedicationAdapter(list, new MedicationAdapter.OnMedicationActionListener() {
                            @Override
                            public void onDelete(MedicationModel model) {
                                database.getReference("Medications").child(model.getId()).removeValue();
                            }

                            @Override
                            public void onStatusChange(MedicationModel model, boolean isTaken) {
                                java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                                updates.put("isTaken", isTaken);
                                updates.put("lastTakenTimestamp", model.getLastTakenTimestamp());
                                database.getReference("Medications").child(model.getId()).updateChildren(updates);
                            }
                        }));
                        binding.medicationSection.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkAndSeedMedications() {
        String uid = FirebaseHelper.getUid();
        if (uid == null) return;

        database.getReference("Medications").orderByChild("userId").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Seed sample medications
                            seedMedication("Amoxicillin", "500mg - 1 Pill", "Morning", uid);
                            seedMedication("Paracetamol", "650mg - 1 Pill", "Afternoon", uid);
                            seedMedication("Vitamin D3", "60K IU - 1 Capsule", "Evening", uid);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void seedMedication(String name, String dosage, String time, String uid) {
        String id = database.getReference("Medications").push().getKey();
        MedicationModel med = new MedicationModel(id, name, dosage, time, "Once Daily", "Take after food", uid);
        if (id != null) {
            database.getReference("Medications").child(id).setValue(med);
        }
    }

    private void initHealthTips() {
        if (viewModel == null) return;
        viewModel.loadHealthTips().observe(this, tips -> {
            if (tips != null) {
                healthTips = tips;
            }
        });
    }

    private void showRandomHealthTip() {
        if (healthTips == null || healthTips.isEmpty()) {
            Toast.makeText(this, "Stay healthy and keep smiling!", Toast.LENGTH_SHORT).show();
            return;
        }

        String randomTip = healthTips.get(random.nextInt(healthTips.size()));

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Daily Health Tip")
                .setMessage(randomTip)
                .setPositiveButton("Got it!", null)
                .setIcon(R.drawable.ic_medical_plus)
                .show();
    }
}
