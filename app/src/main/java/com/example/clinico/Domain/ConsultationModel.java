package com.example.clinico.Domain;

import java.io.Serializable;
import java.util.List;

public class ConsultationModel implements Serializable {
    private String consultationId;
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private String symptoms;
    private String diagnosis;
    private String notes;
    private String recommendedTests;
    private String followUpDate;
    private List<MedicinePrescriptionModel> prescription;
    private long createdAt;

    public ConsultationModel() {}

    public ConsultationModel(String consultationId, String appointmentId, String doctorId, String patientId, String symptoms, String diagnosis, String notes, String recommendedTests, String followUpDate, List<MedicinePrescriptionModel> prescription, long createdAt) {
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.recommendedTests = recommendedTests;
        this.followUpDate = followUpDate;
        this.prescription = prescription;
        this.createdAt = createdAt;
    }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getRecommendedTests() { return recommendedTests; }
    public void setRecommendedTests(String recommendedTests) { this.recommendedTests = recommendedTests; }
    public String getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }
    public List<MedicinePrescriptionModel> getPrescription() { return prescription; }
    public void setPrescription(List<MedicinePrescriptionModel> prescription) { this.prescription = prescription; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
