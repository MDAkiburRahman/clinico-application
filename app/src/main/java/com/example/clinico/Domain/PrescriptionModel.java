package com.example.clinico.Domain;

import java.io.Serializable;

public class PrescriptionModel implements Serializable {
    private String id;
    private String userId;
    private String folderId;
    private String imageUrl;
    private String storagePath;
    private long uploadedAt;

    private String fileName;
    private String fileType; // image or pdf
    private long size;

    public PrescriptionModel() {}

    public PrescriptionModel(String id, String userId, String folderId, String imageUrl, String storagePath, long uploadedAt, String fileName, String fileType, long size) {
        this.id = id;
        this.userId = userId;
        this.folderId = folderId;
        this.imageUrl = imageUrl;
        this.storagePath = storagePath;
        this.uploadedAt = uploadedAt;
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public long getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(long uploadedAt) { this.uploadedAt = uploadedAt; }
}
