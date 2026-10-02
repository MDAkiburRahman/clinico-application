package com.example.clinico.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clinico.Domain.AppointmentModel;
import com.example.clinico.databinding.ViewholderPatientQueueBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class DoctorQueueAdapter extends RecyclerView.Adapter<DoctorQueueAdapter.ViewHolder> {
    private List<AppointmentModel> items;
    private OnQueueActionListener listener;

    public interface OnQueueActionListener {
        void onComplete(AppointmentModel appointment);
        void onCancel(AppointmentModel appointment);
    }

    public DoctorQueueAdapter(List<AppointmentModel> items, OnQueueActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPatientQueueBinding binding = ViewholderPatientQueueBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel item = items.get(position);
        holder.binding.timeTxt.setText(item.getAppointmentTime());
        holder.binding.typeTxt.setText(item.getAppointmentType());

        // Fetch patient data (Name and Phone)
        FirebaseDatabase.getInstance().getReference("Users").child(item.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    
                    holder.binding.patientNameTxt.setText(name);
                    
                    holder.binding.callPatientBtn.setOnClickListener(v -> {
                        if (phone != null && !phone.isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + phone));
                            holder.itemView.getContext().startActivity(intent);
                        } else {
                            android.widget.Toast.makeText(holder.itemView.getContext(), "Phone number not available", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    holder.binding.patientNameTxt.setText("Patient ID: " + item.getUserId());
                    holder.binding.callPatientBtn.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.binding.completeBtn.setOnClickListener(v -> listener.onComplete(item));
        holder.binding.cancelBtn.setOnClickListener(v -> listener.onCancel(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderPatientQueueBinding binding;
        public ViewHolder(ViewholderPatientQueueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
