package com.volunteer.thc.volunteerapp.model;

import java.io.Serializable;

/**
 * Created by poppa on 27.08.2017.
 */

public class ChatSingle extends Message implements Serializable {

    private String uuid;
    private long hour;
    private String receivedBy;
    private boolean received;

    public ChatSingle() {
    }

    public ChatSingle(String sentBy, String receivedBy, String content, String uuid, long hour, boolean received) {
        super(sentBy, uuid, content);
        this.hour = hour;
        this.receivedBy=receivedBy;
        this.received=received;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getHour() {
        return hour;
    }

    public void setHour(long hour) {
        this.hour = hour;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
