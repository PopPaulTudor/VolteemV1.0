package com.volunteer.thc.volunteerapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cristi on 6/14/2017.
 */
public class Organiser {

    private String email, company, city, phone;
    private int eventsnumber, experience;
    private OrganiserRating org_rating;
    private ArrayList<String> events;

    public Organiser(){

    }

    public Organiser(String email, String company, String city, String phone) {
        this.email = email;
        this.company = company;
        this.eventsnumber = 0;
        this.city = city;
        this.org_rating = new OrganiserRating();
        this.experience = 0;
        this.phone = phone;
        this.events = new ArrayList<>();
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getEventsnumber() {
        return eventsnumber;
    }

    public void setEventsnumber(int eventsnumber) {
        this.eventsnumber = eventsnumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public OrganiserRating getOrg_rating() {
        return org_rating;
    }

    public void setOrg_rating(OrganiserRating org_rating) {
        this.org_rating = org_rating;
    }

    public int getExperience() {
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
