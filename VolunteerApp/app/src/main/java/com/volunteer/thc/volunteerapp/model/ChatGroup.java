package com.volunteer.thc.volunteerapp.model;

/**
 * Created by poppa on 03.12.2017.
 */

public class ChatGroup extends Message  {

    private long hour;
    private boolean received;
    private String uuidEvent;

    public ChatGroup() {

    }

    public ChatGroup(String sentBy, String uuid, String content, long hour, boolean received, String uuidEvent) {
        super(sentBy, uuid, content);
        this.hour = hour;
        this.received = received;
        this.uuidEvent = uuidEvent;
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

    public String getUuidEvent() {
        return uuidEvent;
    }

    public void setUuidEvent(String uuidEvent) {
        this.uuidEvent = uuidEvent;
    }
}
