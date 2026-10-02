package com.example.clinico.Domain;

public class BannerModel {
    private String url;
    private String key;

    public BannerModel() {
    }

    public BannerModel(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
