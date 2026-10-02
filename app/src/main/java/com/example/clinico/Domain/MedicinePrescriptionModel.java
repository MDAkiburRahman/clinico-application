package com.example.clinico.Domain;

import java.io.Serializable;

public class MedicinePrescriptionModel implements Serializable {
    private String medicineName;
    private String dosage;
    private String frequency;
    private String time; // e.g. "Morning, Night"
    private String duration; // e.g. "5 days"
    private String instruction; // e.g. "After meal"

    public MedicinePrescriptionModel() {}

    public MedicinePrescriptionModel(String medicineName, String dosage, String frequency, String time, String duration, String instruction) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.time = time;
        this.duration = duration;
        this.instruction = instruction;
    }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
