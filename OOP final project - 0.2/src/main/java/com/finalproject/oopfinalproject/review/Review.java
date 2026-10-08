package com.finalproject.oopfinalproject.review;

import com.finalproject.oopfinalproject.user.Customer;
import com.finalproject.oopfinalproject.vehicle.Vehicle;

public class Review {
    protected String reviewID,comment;
    protected Vehicle vehicle;
    protected Customer customer;
    protected int rating;

    public Review(String reviewID, Customer customer, Vehicle vehicle, String comment, int rating) {
        this.reviewID = reviewID;
        this.customer = customer;
        this.vehicle = vehicle;
        this.comment = comment;
        this.rating = rating;
    }
    public String toFileFormat(){
        return reviewID+","+customer.getCustomerID()+","+vehicle.getVehicleID()+","+comment+","+rating;
    }
}

