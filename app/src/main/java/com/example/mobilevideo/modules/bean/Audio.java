package com.example.mobilevideo.modules.bean;

public class Audio {
    private String codec;

    private int sample_rate;

    private int channel;

    private String profile;

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getCodec() {
        return this.codec;
    }

    public void setSample_rate(int sample_rate) {
        this.sample_rate = sample_rate;
    }

    public int getSample_rate() {
        return this.sample_rate;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getChannel() {
        return this.channel;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return this.profile;
    }
}
