package com.example.clinico.Domain;

import java.io.Serializable;

public class MedicalRecordModel implements Serializable {
    private String recordId;
    private String userId;
    private String doctorId;
    private String doctorName;
    private String diagnosis;
    private String prescription;
    private String reportImageUrl;
    private String doctorNotes;
    private String date;
    private long timestamp;

    public MedicalRecordModel() {
    }

    public MedicalRecordModel(String recordId, String userId, String doctorId, String doctorName, String diagnosis, String prescription, String reportImageUrl, String doctorNotes, String date, long timestamp) {
        this.recordId = recordId;
        this.userId = userId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.reportImageUrl = reportImageUrl;
        this.doctorNotes = doctorNotes;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getReportImageUrl() {
        return reportImageUrl;
    }

    public void setReportImageUrl(String reportImageUrl) {
        this.reportImageUrl = reportImageUrl;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
