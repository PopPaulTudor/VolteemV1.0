package com.volunteer.thc.volunteerapp.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Cristi on 6/14/2017.
 */
public class Event implements Serializable{

    private String created_by, name, location, date, type, description, deadline, eventID;
    private int size;
    private ArrayList<String> registered_volunteers;
    private ArrayList<String> accepted_volunteers;

    public Event(){

    }

    public Event (String created_by, String name, String location, String date, String type, String eventID,
                  String description, String deadline, int size) {
        this.created_by = created_by;
        this.name = name;
        this.location = location;
        this.date = date;
        this.type = type;
        this.description = description;
        this.deadline = deadline;
        this.size = size;
        this.eventID = eventID;
        this.registered_volunteers = new ArrayList<>();
        this.accepted_volunteers = new ArrayList<>();
    }

    public Event (String created_by, String name, String location, String date, String type, String eventID,
                  String description, String deadline, int size, ArrayList<String> registered_volunteers, ArrayList<String> accepted_volunteers) {
        this.created_by = created_by;
        this.name = name;
        this.location = location;
        this.date = date;
        this.type = type;
        this.description = description;
        this.deadline = deadline;
        this.size = size;
        this.eventID = eventID;
        this.registered_volunteers = registered_volunteers;
        this.accepted_volunteers = accepted_volunteers;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public ArrayList<String> getRegistered_volunteers() {
        return registered_volunteers;
    }

    public void setRegistered_volunteers(ArrayList<String> registered_volunteers) {
        this.registered_volunteers = registered_volunteers;
    }

    public ArrayList<String> getAccepted_volunteers() {
        return accepted_volunteers;
    }

    public void setAccepted_volunteers(ArrayList<String> accepted_volunteers) {
        this.accepted_volunteers = accepted_volunteers;
    }
}
