package com.example.clinico.Activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.clinico.Adapter.DateAdapter;
import com.example.clinico.Adapter.TimeSlotAdapter;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.R;
import com.example.clinico.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends BaseActivity {
    private ActivityDetailBinding binding;
    private DoctorsModel doctor;
    private String selectedType = "In-Person";
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        doctor = (DoctorsModel) getIntent().getSerializableExtra("object");
        if (doctor == null) {
            finish();
            return;
        }

        initView();
        setupDoctorData();
        setupAppointmentTypes();
        setupDateSelector();
    }

    private void initView() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.shareBtn.setOnClickListener(v -> shareDoctor());
        
        binding.readMoreBtn.setOnClickListener(v -> {
            if (binding.biographyTxt.getMaxLines() == 3) {
                binding.biographyTxt.setMaxLines(Integer.MAX_VALUE);
                binding.readMoreBtn.setText("Read Less");
            } else {
                binding.biographyTxt.setMaxLines(3);
                binding.readMoreBtn.setText("Read More");
            }
        });

        binding.bookBtn.setOnClickListener(v -> bookAppointment());
    }

    private void setupDoctorData() {
        binding.nameTxt.setText(doctor.getName());
        binding.specialTxt.setText(doctor.getSpecial());
        binding.educationTxt.setText(doctor.getEducation());
        binding.ratingTxt.setText(String.valueOf(doctor.getRating()));
        binding.biographyTxt.setText(doctor.getBiography());

        Glide.with(this)
                .load(doctor.getPicture())
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(binding.img);

        binding.statExperience.statIcon.setImageResource(R.drawable.bottom_btn1);
        binding.statExperience.statValue.setText(doctor.getExperience() + "+");
        binding.statExperience.statLabel.setText("Experience");

        binding.statPatients.statIcon.setImageResource(R.drawable.ic_person);
        binding.statPatients.statValue.setText(String.valueOf(doctor.getPatiens()) + "+");
        binding.statPatients.statLabel.setText("Patients");

        binding.statRating.statIcon.setImageResource(R.drawable.star);
        binding.statRating.statValue.setText(String.valueOf(doctor.getRating()));
        binding.statRating.statLabel.setText("Rating");

        binding.statReviews.statIcon.setImageResource(R.drawable.ic_messages);
        binding.statReviews.statValue.setText(String.valueOf(doctor.getReviewCount()));
        binding.statReviews.statLabel.setText("Reviews");

        binding.summaryDoctorTxt.setText("Doctor: " + doctor.getName());
        binding.summaryFeeTxt.setText("Consultation Fee: ৳" + doctor.getPrice());
    }

    private void setupAppointmentTypes() {
        updateTypeUI();
        binding.typeInPerson.setOnClickListener(v -> {
            selectedType = "In-Person";
            updateTypeUI();
        });
        binding.typeOnline.setOnClickListener(v -> {
            selectedType = "Online Consultation";
            updateTypeUI();
        });
    }

    private void updateTypeUI() {
        int activeColor = getResources().getColor(R.color.premium_blue_primary);
        int inactiveColor = getResources().getColor(R.color.premium_text_gray);

        if (selectedType.equals("In-Person")) {
            binding.typeInPerson.setBackgroundResource(R.drawable.nav_pill_bg);
            binding.iconInPerson.setImageTintList(ColorStateList.valueOf(activeColor));
            binding.labelInPerson.setTextColor(activeColor);

            binding.typeOnline.setBackgroundResource(R.drawable.premium_card_bg);
            binding.iconOnline.setImageTintList(ColorStateList.valueOf(inactiveColor));
            binding.labelOnline.setTextColor(getResources().getColor(R.color.premium_text_dark));
        } else {
            binding.typeOnline.setBackgroundResource(R.drawable.nav_pill_bg);
            binding.iconOnline.setImageTintList(ColorStateList.valueOf(activeColor));
            binding.labelOnline.setTextColor(activeColor);

            binding.typeInPerson.setBackgroundResource(R.drawable.premium_card_bg);
            binding.iconInPerson.setImageTintList(ColorStateList.valueOf(inactiveColor));
            binding.labelInPerson.setTextColor(getResources().getColor(R.color.premium_text_dark));
        }
        updateSummary();
    }

    private void setupDateSelector() {
        List<String> days = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);

        binding.monthTxt.setText(new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(calendar.getTime()));

        for (int i = 0; i < 14; i++) {
            days.add(dayFormat.format(calendar.getTime()).toUpperCase());
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        binding.dateView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DateAdapter adapter = new DateAdapter(days, dates, date -> {
            selectedDate = date; // day of month
            selectedTime = ""; // Reset time
            
            // Get full date string in yyyy-MM-dd format to match Firebase keys
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date));
            String fullDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.getTime());
            
            updateTimeSlots(fullDate);
            updateSummary();
        });
        binding.dateView.setAdapter(adapter);
    }

    private Date getDateFromDay(String dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayOfMonth));
        return cal.getTime();
    }

    private void updateTimeSlots(String fullDate) {
        Map<String, List<DoctorsModel.TimeSlot>> availability = doctor.getAvailability();
        List<String> slots = new ArrayList<>();

        if (availability != null && availability.containsKey(fullDate)) {
            List<DoctorsModel.TimeSlot> timeSlots = availability.get(fullDate);
            if (timeSlots != null) {
                for (DoctorsModel.TimeSlot slot : timeSlots) {
                    if (slot.isAvailable()) {
                        slots.add(slot.getTime());
                    }
                }
            }
        }

        if (slots.isEmpty()) {
            slots.add("No slots available");
        }

        binding.timeView.setLayoutManager(new GridLayoutManager(this, 3));
        TimeSlotAdapter adapter = new TimeSlotAdapter(slots, time -> {
            if (!time.contains("No slots")) {
                selectedTime = time;
                updateSummary();
            }
        });
        binding.timeView.setAdapter(adapter);
    }

    private void updateSummary() {
        binding.summaryDateTimeTxt.setText("Date: " + selectedDate + " | Time: " + (selectedTime.isEmpty() ? "-" : selectedTime));
    }

    private void bookAppointment() {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        String appointmentId = FirebaseDatabase.getInstance().getReference("Appointments").push().getKey();

        AppointmentModel appointment = new AppointmentModel();
        appointment.setAppointmentId(appointmentId);
        appointment.setUserId(userId);
        appointment.setDoctorId(doctor.getKey());
        appointment.setDoctorName(doctor.getName());
        appointment.setDoctorImage(doctor.getPicture());
        appointment.setSpecialization(doctor.getSpecial());
        appointment.setAppointmentDate(selectedDate + " " + binding.monthTxt.getText().toString());
        appointment.setAppointmentTime(selectedTime);
        appointment.setAppointmentType(selectedType);
        appointment.setConsultationFee(doctor.getPrice());
        appointment.setHospital(doctor.getLocation());
        
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("appointment", appointment);
        startActivity(intent);
    }

    private void shareDoctor() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Doctor Profile");
        intent.putExtra(Intent.EXTRA_TEXT, "Check out " + doctor.getName() + " on Clinico app!");
        startActivity(Intent.createChooser(intent, "Share via"));
    }
}
