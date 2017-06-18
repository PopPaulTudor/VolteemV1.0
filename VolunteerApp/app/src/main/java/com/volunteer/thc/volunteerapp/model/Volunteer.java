package com.volunteer.thc.volunteerapp.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Cristi on 6/14/2017.
 */
public class Volunteer {

    private String firstname, lastname, email, city, phone, age, experience;

    public Volunteer(){ ///constructor is empty to be able to call dataSnapshot on this class

    }

    public Volunteer(String firstname, String lastname, String email, String age, String city, String phone){

        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.age = age;
        this.city = city;
        this.phone = phone;
        this.experience = ""+0;

    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("age", age);
        result.put("city", city);
        result.put("email", email);
        result.put("experience", experience);
        result.put("firstname", firstname);
        result.put("lastname", lastname);
        result.put("phone", phone);

        return result;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
