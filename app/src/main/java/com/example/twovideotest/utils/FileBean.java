package com.example.twovideotest.utils;

public class FileBean {
    public FileBean(String name, long date) {
        this.name = name;
        this.date = date;
    }

    private String name;
    private long date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
