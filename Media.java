package com.mediaplayer.model;

public class Media {
    private int id;
    private String title;
    private String filePath;
    private String type; // "video" or "audio"
    private int userId;

    public Media() {}

    public Media(String title, String filePath, String type, int userId) {
        this.title = title;
        this.filePath = filePath;
        this.type = type;
        this.userId = userId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
