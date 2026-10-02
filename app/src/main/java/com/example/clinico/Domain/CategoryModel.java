package com.example.clinico.Domain;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private int Id;
    private String Name;
    private String Picture;
    private String key;


    public CategoryModel() {
    }

    public CategoryModel(int id, String name, String picture) {
        Id = id;
        Name = name;
        Picture = picture;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
