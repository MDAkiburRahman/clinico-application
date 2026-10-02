package com.example.clinico.Domain;

import java.io.Serializable;

public class PaymentModel implements Serializable {
    private String paymentId;
    private String appointmentId;
    private String userId;
    private double amount;
    private String paymentMethod; // Bkash, Nagad, Rocket, Stripe, Cash
    private String status; // Pending, Completed, Failed
    private long timestamp;

    public PaymentModel() {
    }

    public PaymentModel(String paymentId, String appointmentId, String userId, double amount, String paymentMethod, String status, long timestamp) {
        this.paymentId = paymentId;
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
