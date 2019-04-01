package com.volunteer.thc.volunteerapp.model;



/**
 * Created by Cristi on 6/14/2017.
 */
public class Volunteer {

    private String firstname, lastname, email, city, phone, gender;
    private int experience;
    private long birthdate;

    public Volunteer() { ///constructor is empty to be able to call dataSnapshot on this class

    }

    public Volunteer(String firstname, String lastname, String email, long birthdate, String city, String phone, String gender) {

        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.birthdate = birthdate;
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

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }
}
