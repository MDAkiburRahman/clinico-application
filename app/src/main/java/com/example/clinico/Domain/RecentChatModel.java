package com.example.clinico.Domain;

import java.io.Serializable;

public class RecentChatModel implements Serializable {
    private String id;
    private String name;
    private String lastMessage;
    private long timestamp;
    private String picture;

    public RecentChatModel() {}

    public RecentChatModel(String id, String name, String lastMessage, long timestamp, String picture) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.picture = picture;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
}
