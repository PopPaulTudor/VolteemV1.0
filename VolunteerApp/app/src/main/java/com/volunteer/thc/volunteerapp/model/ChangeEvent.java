package com.volunteer.thc.volunteerapp.model;

/**
 * Created by poppa on 22.10.2017.
 */

public class ChangeEvent {

    private String title;
    private String content;
    private Event  event;


    private boolean notified;

    public ChangeEvent(String title, String content, Event event, boolean notified) {
        this.title = title;
        this.content = content;
        this.event = event;
        this.notified = notified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

}
