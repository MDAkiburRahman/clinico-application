package com.example.clinico.Domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DoctorsModel implements Serializable {
    private String Address;
    private String Biography;
    private int Id;
    private String Name;
    private String Picture;
    private String Special;
    private int Experience;
    private Object Price;
    private String Date;
    private String Time;
    private String Location;
    private Object Mobile;
    private Object Patiens;
    private double Rating;
    private int ReviewCount;
    private String Education;
    private String Languages;
    private String Site;
    private String Certificates;
    private int MaxAppointments;
    private boolean isFeatured;
    private boolean isDisabled;
    private boolean isOnline;
    private String key;
    private String email;
    private String uid;
    private Map<String, List<TimeSlot>> Availability; // Map<DateString, List<TimeSlot>>

    public static class TimeSlot implements Serializable {
        private String time;
        private boolean isAvailable;

        public TimeSlot() {}
        public TimeSlot(String time, boolean isAvailable) {
            this.time = time;
            this.isAvailable = isAvailable;
        }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
    }

    public DoctorsModel() {
    }

    public String getAddress() { return Address; }
    public void setAddress(String address) { Address = address; }

    public String getBiography() { return Biography; }
    public void setBiography(String biography) { Biography = biography; }

    public int getId() { return Id; }
    public void setId(int id) { Id = id; }

    public String getName() { return Name; }
    public void setName(String name) { Name = name; }

    public String getPicture() { return Picture; }
    public void setPicture(String picture) { Picture = picture; }

    public String getSpecial() { return Special; }
    public void setSpecial(String special) { Special = special; }

    public int getExperience() { return Experience; }
    public void setExperience(int experience) { Experience = experience; }

    public String getPrice() { return Price != null ? String.valueOf(Price) : "0"; }
    public void setPrice(Object price) { Price = price; }

    public String getLocation() { return Location; }
    public void setLocation(String location) { Location = location; }

    public String getMobile() { return Mobile != null ? String.valueOf(Mobile) : ""; }
    public void setMobile(Object mobile) { Mobile = mobile; }

    public String getPatiens() { return Patiens != null ? String.valueOf(Patiens) : "0"; }
    public void setPatiens(Object patiens) { Patiens = patiens; }

    public double getRating() { return Rating; }
    public void setRating(double rating) { Rating = rating; }

    public int getReviewCount() { return ReviewCount; }
    public void setReviewCount(int reviewCount) { ReviewCount = reviewCount; }

    public String getEducation() { return Education; }
    public void setEducation(String education) { Education = education; }

    public String getLanguages() { return Languages; }
    public void setLanguages(String languages) { Languages = languages; }

    public String getSite() { return Site; }
    public void setSite(String site) { Site = site; }

    public String getCertificates() { return Certificates; }
    public void setCertificates(String certificates) { Certificates = certificates; }

    public int getMaxAppointments() { return MaxAppointments; }
    public void setMaxAppointments(int maxAppointments) { MaxAppointments = maxAppointments; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public boolean isDisabled() { return isDisabled; }
    public void setDisabled(boolean disabled) { isDisabled = disabled; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public Map<String, List<TimeSlot>> getAvailability() { return Availability; }
    public void setAvailability(Map<String, List<TimeSlot>> availability) { Availability = availability; }
}
