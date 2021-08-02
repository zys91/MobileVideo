package com.example.mobilevideo.modules.bean;

import java.math.BigInteger;

public class Streams {
    private int id;

    private String name;

    private int vhost;

    private String app;

    private BigInteger live_ms;

    private int clients;

    private int frames;

    private BigInteger send_bytes;

    private BigInteger recv_bytes;

    private Kbps kbps;

    private Publish publish;

    private Video video;

    private Audio audio;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setVhost(int vhost) {
        this.vhost = vhost;
    }

    public int getVhost() {
        return this.vhost;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return this.app;
    }

    public void setLive_ms(BigInteger live_ms) {
        this.live_ms = live_ms;
    }

    public BigInteger getLive_ms() {
        return this.live_ms;
    }

    public void setClients(int clients) {
        this.clients = clients;
    }

    public int getClients() {
        return this.clients;
    }

    public void setFrames(int frames) {
        this.frames = frames;
    }

    public int getFrames() {
        return this.frames;
    }

    public void setSend_bytes(BigInteger send_bytes) {
        this.send_bytes = send_bytes;
    }

    public BigInteger getSend_bytes() {
        return this.send_bytes;
    }

    public void setRecv_bytes(BigInteger recv_bytes) {
        this.recv_bytes = recv_bytes;
    }

    public BigInteger getRecv_bytes() {
        return this.recv_bytes;
    }

    public void setKbps(Kbps kbps) {
        this.kbps = kbps;
    }

    public Kbps getKbps() {
        return this.kbps;
    }

    public void setPublish(Publish publish) {
        this.publish = publish;
    }

    public Publish getPublish() {
        return this.publish;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Video getVideo() {
        return this.video;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public Audio getAudio() {
        return this.audio;
    }
}
