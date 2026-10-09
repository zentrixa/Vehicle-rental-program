package com.finalproject.oopfinalproject.review;

import com.finalproject.oopfinalproject.User.Customer;
import com.finalproject.oopfinalproject.vehicle.Vehicle;
import com.finalproject.oopfinalproject.booking.Booking;

public class VerifiedReview extends Review {
    private Booking booking;

    public VerifiedReview(String reviewID, Customer customer, Vehicle vehicle, String comment, int rating,Booking booking) {
        super(reviewID, customer, vehicle,comment,rating);
        this.booking = booking;

    }

    @Override
    public String toFileFormat() {
        return super.toFileFormat()+","+booking.getBookingID() +","+"VERIFIED";
    }
}
