package com.example.mobilevideo.modules.bean;

import java.util.List;

public class JsonBean {
    private int code;

    private int server;

    private List<Streams> streams;

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public void setServer(int server) {
        this.server = server;
    }

    public int getServer() {
        return this.server;
    }

    public void setStreams(List<Streams> streams) {
        this.streams = streams;
    }

    public List<Streams> getStreams() {
        return this.streams;
    }
}

