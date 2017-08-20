package com.volunteer.thc.volunteerapp.model;

/**
 * Created by Cristi on 8/20/2017.
 */

public class RegisteredUser {
    private String status;
    private String id;
    private String flag;

    public RegisteredUser() {

    }

    public RegisteredUser(String status, String id, String flag) {
        this.status = status;
        this.id = id;
        this.flag = flag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
