package com.example.clinico.Domain;

import java.io.Serializable;

public class ConsultantModel implements Serializable {
    private String id;
    private String name;
    private String special;
    private String phone;
    private String picture;
    private boolean isOnline;
    private String bio;
    private int experience;

    public ConsultantModel() {}

    public ConsultantModel(String id, String name, String special, String phone, String picture, boolean isOnline, String bio, int experience) {
        this.id = id;
        this.name = name;
        this.special = special;
        this.phone = phone;
        this.picture = picture;
        this.isOnline = isOnline;
        this.bio = bio;
        this.experience = experience;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecial() { return special; }
    public void setSpecial(String special) { this.special = special; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
}
