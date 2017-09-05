package com.volunteer.thc.volunteerapp.model;

import java.io.Serializable;

/**
 * Created by poppa on 27.08.2017.
 */

public class Chat extends Message implements Serializable {

    private String uuid;
    private long hour;
    private boolean received;

    public Chat() {
    }

    public Chat(String sentBy, String receivedBy, String content, String uuid, long hour,boolean received) {
        super(sentBy, receivedBy, content);
        this.uuid = uuid;
        this.hour = hour;
        this.received=received;
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

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
