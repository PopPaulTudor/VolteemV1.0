package com.volunteer.thc.volunteerapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cristi on 6/14/2017.
 */
public class Organiser {

    private String email, company, eventsnumber, city, rating, experience, phone;
    private ArrayList<String> events;

    public Organiser(){

    }

    public Organiser(String email, String company, String city, String phone) {
        this.email = email;
        this.company = company;
        this.eventsnumber = ""+0;
        this.city = city;
        this.rating = ""+0;
        this.experience = ""+0;
        this.phone = phone;
        this.events = new ArrayList<>();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("company", company);
        result.put("city", city);
        result.put("email", email);
        result.put("experience", experience);
        result.put("rating", rating);
        result.put("eventsnumber", eventsnumber);
        result.put("phone", phone);
        result.put("events", events);

        return result;
    }


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEventsnumber() {
        return eventsnumber;
    }

    public void setEventsnumber(String eventsnumber) {
        this.eventsnumber = eventsnumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getExperience() {
        return experience;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<String> events) {
        this.events = events;
    }
}
