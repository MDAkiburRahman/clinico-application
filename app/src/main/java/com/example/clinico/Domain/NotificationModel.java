package com.example.clinico.Domain;

import java.io.Serializable;

public class NotificationModel implements Serializable {
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String appointmentId;
    private boolean read;
    private long createdAt;

    public NotificationModel() {}

    public NotificationModel(String notificationId, String userId, String title, String message, String appointmentId, boolean read, long createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.appointmentId = appointmentId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
