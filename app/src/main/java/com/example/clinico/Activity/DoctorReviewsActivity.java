package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clinico.Adapter.DoctorReviewAdapter;
import com.example.clinico.Domain.ReviewModel;
import com.example.clinico.Helper.FirebaseHelper;
import com.example.clinico.databinding.ActivityDoctorReviewsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoctorReviewsActivity extends BaseActivity {
    private ActivityDoctorReviewsBinding binding;
    private List<ReviewModel> reviewList = new ArrayList<>();
    private DoctorReviewAdapter adapter;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        doctorId = getIntent().getStringExtra("doctorId");
        if (doctorId == null) {
            finish();
            return;
        }

        initView();
        loadReviews();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.reviewsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorReviewAdapter(reviewList);
        binding.reviewsRv.setAdapter(adapter);
    }

    private void loadReviews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getReviewsRef().orderByChild("doctorId").equalTo(doctorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();
                        float totalRating = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ReviewModel review = ds.getValue(ReviewModel.class);
                            if (review != null) {
                                reviewList.add(review);
                                totalRating += review.getRating();
                            }
                        }
                        Collections.reverse(reviewList);
                        adapter.notifyDataSetChanged();

                        if (!reviewList.isEmpty()) {
                            float avg = totalRating / reviewList.size();
                            binding.ratingTxt.setText(String.format("%.1f", avg));
                            binding.overallRatingBar.setRating(avg);
                            binding.reviewCountTxt.setText("Based on " + reviewList.size() + " reviews");
                        }

                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
