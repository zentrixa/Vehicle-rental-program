package com.finalproject.oopfinalproject.Review;

import com.finalproject.oopfinalproject.User.Customer;
import com.finalproject.oopfinalproject.Vehicle.Vehicle;

public class Review {
    private String reviewID,comment;
    private Vehicle Vehicle;
    protected Customer customer;
    private int rating;

    public Review(String reviewID, Customer customer, Vehicle vehicle, String comment, int rating) {
        this.reviewID = reviewID;
        this.customer = customer;
        this.Vehicle = vehicle;
        this.comment = comment;
        this.rating = rating;
    }

    public String toFileFormat(){
        String cleanComment=(this.comment!=null)? comment.replace(","," "):"";
        return reviewID+","+customer.getId()+","+Vehicle.getVehicleId()+","+cleanComment+","+rating;
    }

}

