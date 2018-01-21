package com.volunteer.thc.volunteerapp.model;

import java.io.Serializable;

/**
 * Created by poppa on 25.08.2017.
 */

public class Message implements Serializable {

    private String sentBy;
    private String uuid;
    private String content;

    public Message() {
    }

    public Message(String sentBy, String uuid, String content) {
        this.sentBy = sentBy;
        this.uuid = uuid;
        this.content = content;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
