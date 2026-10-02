package com.example.clinico.Domain;

import java.io.Serializable;

public class PrescriptionFolderModel implements Serializable {
    private String id;
    private String patientId;
    private String folderName;
    private long createdAt;

    public PrescriptionFolderModel() {}

    public PrescriptionFolderModel(String id, String patientId, String folderName, long createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.folderName = folderName;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
