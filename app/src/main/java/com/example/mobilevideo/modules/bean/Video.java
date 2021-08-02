package com.example.mobilevideo.modules.bean;

public class Video {
    private String codec;

    private String profile;

    private String level;

    private int width;

    private int height;

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getCodec() {
        return this.codec;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return this.profile;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return this.level;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return this.width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return this.height;
    }
}
