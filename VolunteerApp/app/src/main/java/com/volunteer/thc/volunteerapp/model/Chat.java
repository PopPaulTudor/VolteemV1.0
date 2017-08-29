package com.volunteer.thc.volunteerapp.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by poppa on 27.08.2017.
 */

public class Chat extends Message implements Serializable {

    private String uuid;

    public Chat() {
    }

    public Chat(String sentBy, String receivedBy, String content,String uuid) {
        super(sentBy, receivedBy, content);
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
