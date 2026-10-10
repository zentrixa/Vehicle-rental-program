package com.finalproject.oopfinalproject.Review;

import com.finalproject.oopfinalproject.User.Customer;
import com.finalproject.oopfinalproject.Vehicle.Vehicle;
import com.finalproject.oopfinalproject.Booking.Booking;

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
