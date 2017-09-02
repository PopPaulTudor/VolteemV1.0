package com.volunteer.thc.volunteerapp.model;

import java.io.Serializable;

/**
 * Created by poppa on 27.08.2017.
 */

public class Chat extends Message implements Serializable {

    private String uuid;
    private long hour;

    public Chat() {
    }

    public Chat(String sentBy, String receivedBy, String content, String uuid, long hour) {
        super(sentBy, receivedBy, content);
        this.uuid = uuid;
        this.hour = hour;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getHour() {
        return hour;
    }

    public void setHour(long hour) {
        this.hour = hour;
    }
}
