package com.volunteer.thc.volunteerapp.model;

/**
 * Created by Cristi on 8/25/2017.
 */

public class NewsMessage extends Message {

    public static final int ACCEPT = 1, FEEDBACK = 2, EVENT_DELETED = 3, REGISTERED = 4, VOLUNTEER_LEFT = 5;
    private int type;
    private boolean notified;
    private boolean starred;
    private String newsID, eventID;
    private long expireDate;

    public NewsMessage() {

    }

    public NewsMessage(long expireDate, String newsID, String eventID, String sentBy, String receivedBy, String content, final int type, boolean notified, boolean starred) {
        super(sentBy, receivedBy, content);
        this.type = type;
        this.notified = notified;
        this.starred = starred;
        this.newsID = newsID;
        this.expireDate = expireDate;
        this.eventID = eventID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public String getNewsID() {
        return newsID;
    }

    public void setNewsID(String newsID) {
        this.newsID = newsID;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
