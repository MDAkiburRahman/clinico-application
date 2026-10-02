package com.example.clinico.Domain;

import java.io.Serializable;

public class EmergencyContactModel implements java.io.Serializable {
    private String id;
    private String name;
    private String phone;
    private String relation;

    public EmergencyContactModel() {
    }

    public EmergencyContactModel(String id, String name, String phone, String relation) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.relation = relation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
