package com.example.clinico.Activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.Utils.Constants;
import com.example.clinico.Utils.NetworkUtils;
import com.example.clinico.Utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import androidx.appcompat.app.AppCompatDelegate;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import com.example.clinico.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

public abstract class BaseActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    protected FirebaseDatabase database;
    protected SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            mAuth = FirebaseHelper.getAuth();
            database = FirebaseHelper.getDatabase();
            sessionManager = new SessionManager(this);
            applyTheme();
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("BaseActivity", "Firebase initialization failed", e);
        }
    }

    protected boolean checkInternet() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoInternetDialog();
            return false;
        }
        return true;
    }

    private boolean isDialogShowing = false;

    protected void showNoInternetDialog() {
        if (isDialogShowing) return;
        isDialogShowing = true;

        new MaterialAlertDialogBuilder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                    isDialogShowing = false;
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        onInternetRestored();
                    } else {
                        showNoInternetDialog();
                    }
                })
                .setNegativeButton("Close", (dialog, which) -> {
                    isDialogShowing = false;
                    finish();
                })
                .setOnCancelListener(dialog -> isDialogShowing = false)
                .setCancelable(false)
                .show();
    }

    protected void onInternetRestored() {
        // Override this in child activities to refresh data
    }

    protected void showLoading() {
        // Implement global loading if needed
    }

    protected void hideLoading() {
        // Implement global loading if needed
    }

    protected void applyTheme() {
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    protected void loadAppLogo(ImageView imageView) {
        database.getReference(Constants.NODE_APP_CONFIG).child("logoUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String url = snapshot.getValue(String.class);
                    if (url != null && !url.isEmpty() && !isDestroyed()) {
                        Glide.with(BaseActivity.this).load(url).placeholder(R.mipmap.ic_launcher).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
