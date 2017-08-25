package com.volunteer.thc.volunteerapp.model;

/**
 * Created by poppa on 25.08.2017.
 */

public class Message {

    String sentBy;
    String receivedBy;
    String content;

    public Message(String sentBy, String receivedBy, String content) {
        this.sentBy = sentBy;
        this.receivedBy = receivedBy;
        this.content = content;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
