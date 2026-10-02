package com.example.clinico.Domain;

import java.io.Serializable;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class MedicationModel implements Serializable {
    private String id;
    private String name;
    private String dosage;
    private String time;
    private String frequency;
    private String notes;
    private boolean isTaken;
    private String userId;
    private long lastTakenTimestamp;

    public MedicationModel() {}

    public MedicationModel(String id, String name, String dosage, String time, String frequency, String notes, String userId) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.frequency = frequency;
        this.notes = notes;
        this.isTaken = false;
        this.userId = userId;
    }

    @PropertyName("isTaken")
    public boolean isIsTaken() {
        return isTaken;
    }

    @PropertyName("isTaken")
    public void setIsTaken(boolean taken) {
        isTaken = taken;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getLastTakenTimestamp() {
        return lastTakenTimestamp;
    }

    public void setLastTakenTimestamp(long lastTakenTimestamp) {
        this.lastTakenTimestamp = lastTakenTimestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean shouldReset() {
        if (!isTaken) return false;
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        return (currentTime - lastTakenTimestamp) >= twentyFourHoursInMillis;
    }
}
