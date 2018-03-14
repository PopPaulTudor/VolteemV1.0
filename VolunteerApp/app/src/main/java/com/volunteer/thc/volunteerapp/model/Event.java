package com.volunteer.thc.volunteerapp.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Cristi on 6/14/2017.
 */
public class Event implements Serializable {

    private String createdBy;
    private String name;
    private String location;
    private String type;
    private String description;
    private String eventId;
    private long startDate, finishDate, deadline;
    private int size;
    private ArrayList<String> registeredVolunteers;
    private ArrayList<String> acceptedVolunteers;

    public Event() {

    }

    public Event(String createdBy, String name, String location, long startDate, long finishDate, String type, String eventId,
                 String description, long deadline, int size) {
        this.createdBy = createdBy;
        this.name = name;
        this.location = location;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.type = type;
        this.description = description;
        this.deadline = deadline;
        this.size = size;
        this.eventId = eventId;
        this.registeredVolunteers = new ArrayList<>();
        this.acceptedVolunteers = new ArrayList<>();
    }

    public Event(String createdBy, String name, String location, long startDate, long finishDate, String type, String eventId,
                 String description, long deadline, int size, ArrayList<String> registeredVolunteers, ArrayList<String> acceptedVolunteers) {
        this.createdBy = createdBy;
        this.name = name;
        this.location = location;
        this.type = type;
        this.finishDate = finishDate;
        this.startDate = startDate;
        this.description = description;
        this.deadline = deadline;
        this.size = size;
        this.eventId = eventId;
        this.registeredVolunteers = registeredVolunteers;
        this.acceptedVolunteers = acceptedVolunteers;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(long finishDate) {
        this.finishDate = finishDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public ArrayList<String> getRegisteredVolunteers() {
        return registeredVolunteers;
    }

    public void setRegisteredVolunteers(ArrayList<String> registeredVolunteers) {
        this.registeredVolunteers = registeredVolunteers;
    }

    public ArrayList<String> getAcceptedVolunteers() {
        return acceptedVolunteers;
    }

    public void setAcceptedVolunteers(ArrayList<String> acceptedVolunteers) {
        this.acceptedVolunteers = acceptedVolunteers;
    }

}
