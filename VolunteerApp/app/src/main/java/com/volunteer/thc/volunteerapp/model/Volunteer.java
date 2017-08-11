package com.volunteer.thc.volunteerapp.model;

/**
 * Created by Cristi on 6/14/2017.
 */
public class Volunteer {

    private String firstname, lastname, email, city, phone, gender;
    private int age, experience;

    public Volunteer() { ///constructor is empty to be able to call dataSnapshot on this class

    }

    public Volunteer(String firstname, String lastname, String email, int age, String city, String phone, String gender) {

        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.age = age;
        this.city = city;
        this.phone = phone;
        this.experience = 0;
        this.gender = gender;
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

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
