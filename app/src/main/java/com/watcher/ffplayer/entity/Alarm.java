package com.watcher.ffplayer.entity;

import android.app.Activity;

import java.io.Serializable;

public class Alarm implements Serializable {
    public   String type;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Class<?> warnActivity;
    public String time;
    public String toString()
    {
        return type+";"+time+";"+warnActivity;
    }
}
