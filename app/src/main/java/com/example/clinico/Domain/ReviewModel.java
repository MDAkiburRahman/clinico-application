package com.example.clinico.Domain;

import java.io.Serializable;

public class ReviewModel implements Serializable {
    private String reviewId;
    private String userId;
    private String userName;
    private String userImage;
    private String doctorId;
    private float rating;
    private String comment;
    private long timestamp;
    private String appointmentId;

    public ReviewModel() {
    }

    public ReviewModel(String reviewId, String userId, String userName, String userImage, String doctorId, float rating, String comment, long timestamp, String appointmentId) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.doctorId = doctorId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
        this.appointmentId = appointmentId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
