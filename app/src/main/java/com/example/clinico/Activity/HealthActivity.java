package com.example.clinico.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityHealthBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.Locale;

public class HealthActivity extends BaseActivity {
    private ActivityHealthBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHealthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadHealthData();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());

        // Setup Gender Spinner
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.genderSpinner.setAdapter(genderAdapter);

        binding.calculateBmiBtn.setOnClickListener(v -> calculateBMI());
        binding.calculateCalorieBtn.setOnClickListener(v -> calculateCalories());
    }

    private void calculateBMI() {
        String weightStr = binding.weightEt.getText().toString();
        String heightStr = binding.heightEt.getText().toString();

        if (weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please enter weight and height", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr) / 100.0; // cm to m

        if (height == 0) return;

        double bmi = weight / (height * height);
        binding.bmiValueTxt.setText(String.format(Locale.getDefault(), "BMI: %.1f", bmi));
        binding.bmiResultLayout.setVisibility(View.VISIBLE);

        String status;
        String guidance;
        int color;

        if (bmi < 18.5) {
            status = "Underweight";
            color = getResources().getColor(R.color.OrangeBtn);
            guidance = "Suggestions: Increase calorie intake with nutrient-dense foods. Focus on muscle-building exercises.";
        } else if (bmi < 25) {
            status = "Normal";
            color = getResources().getColor(R.color.premium_success);
            guidance = "Suggestions: You are in a healthy range! Maintain your current lifestyle with a balanced diet and regular activity.";
        } else if (bmi < 30) {
            status = "Overweight";
            color = getResources().getColor(R.color.OrangeBtn);
            guidance = "Suggestions: Focus on portion control and increasing cardiovascular exercise. Reduce sugary drinks and processed foods.";
        } else {
            status = "Obese";
            color = getResources().getColor(R.color.red);
            guidance = "Suggestions: Consult a nutritionist for a structured weight loss plan. Prioritize daily movement and fiber-rich foods.";
        }

        binding.bmiStatusTxt.setText("Status: " + status);
        binding.bmiStatusTxt.setTextColor(color);
        binding.bmiGuidanceTxt.setText(guidance);

        // Auto calculate water goal as well
        calculateWaterIntake(weight);
    }

    private void calculateCalories() {
        String weightStr = binding.weightEt.getText().toString();
        String heightStr = binding.heightEt.getText().toString();
        String ageStr = binding.ageEt.getText().toString();

        if (weightStr.isEmpty() || heightStr.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields in both cards", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);
        int age = Integer.parseInt(ageStr);
        boolean isMale = binding.genderSpinner.getSelectedItemPosition() == 0;

        // Mifflin-St Jeor Equation
        double bmr;
        if (isMale) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        double maintenance = bmr * 1.375; // Lightly active multiplier

        binding.bmrTxt.setText(String.format(Locale.getDefault(), "BMR (Basal Metabolic Rate): %.0f kcal/day", bmr));
        binding.calorieTxt.setText(String.format(Locale.getDefault(), "Maintenance Calories: %.0f kcal/day", maintenance));
        
        // Ideal Weight (Devine Formula)
        double heightInInches = height / 2.54;
        double baseWeight = isMale ? 50.0 : 45.5;
        double idealWeight = baseWeight + (2.3 * Math.max(0, heightInInches - 60));
        
        binding.idealWeightTxt.setText(String.format(Locale.getDefault(), "Estimated Ideal Weight: %.1f kg", idealWeight));
        binding.calorieResultLayout.setVisibility(View.VISIBLE);
    }

    private void calculateWaterIntake(double weight) {
        double liters = weight * 0.033;
        binding.waterResultTxt.setText(String.format(Locale.getDefault(), "%.1f Liters / Day", liters));
    }

    private void loadHealthData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference("HealthData").child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            String weight = snapshot.child("weight").getValue(String.class);
                            String height = snapshot.child("height").getValue(String.class);
                            String age = snapshot.child("age").getValue(String.class);

                            if (weight != null) binding.weightEt.setText(weight.replaceAll("[^0-9.]", ""));
                            if (height != null) binding.heightEt.setText(height.replaceAll("[^0-9.]", ""));
                            if (age != null) binding.ageEt.setText(age.replaceAll("[^0-9.]", ""));
                            
                            if (weight != null && height != null) {
                                calculateBMI();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
