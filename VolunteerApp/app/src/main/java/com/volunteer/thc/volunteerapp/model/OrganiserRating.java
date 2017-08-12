package com.volunteer.thc.volunteerapp.model;

/**
 * Created by Cristi on 8/12/2017.
 */

public class OrganiserRating {
    private double rating;
    private int reviews_number;

    public OrganiserRating() {}

    public OrganiserRating(int parameter) {

        this.rating = 0;
        this.reviews_number = 0;
    }

    public void calculateNewRating(int rating) {
        this.rating = ((this.rating * reviews_number) + rating) / (double) (reviews_number + 1);
        ++reviews_number;
    }

    public int getReviews_number() {
        return reviews_number;
    }

    public void setReviews_number(int reviews_number) {
        this.reviews_number = reviews_number;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
