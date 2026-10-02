package com.example.clinico.Adapter;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clinico.Domain.DoctorsModel;
import com.example.clinico.databinding.ViewholderAvailabilityDayBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AvailabilityDayAdapter extends RecyclerView.Adapter<AvailabilityDayAdapter.ViewHolder> {

    private List<String> days;
    private Map<String, List<DoctorsModel.TimeSlot>> availability;
    private OnAvailabilityChangeListener listener;

    public interface OnAvailabilityChangeListener {
        void onChanged();
    }

    public AvailabilityDayAdapter(List<String> days, Map<String, List<DoctorsModel.TimeSlot>> availability, OnAvailabilityChangeListener listener) {
        this.days = days;
        this.availability = availability;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAvailabilityDayBinding binding = ViewholderAvailabilityDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String day = days.get(position);
        holder.binding.dayTxt.setText(day);

        List<DoctorsModel.TimeSlot> slots = availability.get(day);
        if (slots == null) {
            slots = new ArrayList<>();
            availability.put(day, slots);
        }

        holder.binding.daySwitch.setChecked(!slots.isEmpty());
        // Simple logic: if switch off, we'll clear slots on save or just leave it.

        holder.binding.slotsRv.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 3));
        List<DoctorsModel.TimeSlot> finalSlots = slots;
        AvailabilitySlotAdapter slotAdapter = new AvailabilitySlotAdapter(slots, pos -> {
            finalSlots.remove(pos);
            notifyItemChanged(position);
            listener.onChanged();
        });
        holder.binding.slotsRv.setAdapter(slotAdapter);

        holder.binding.addSlotBtn.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker = new TimePickerDialog(holder.itemView.getContext(), (timePicker, selectedHour, selectedMinute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", 
                        (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12, 
                        selectedMinute, 
                        selectedHour < 12 ? "AM" : "PM");
                finalSlots.add(new DoctorsModel.TimeSlot(time, true));
                notifyItemChanged(position);
                listener.onChanged();
            }, hour, minute, false);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderAvailabilityDayBinding binding;

        public ViewHolder(ViewholderAvailabilityDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
