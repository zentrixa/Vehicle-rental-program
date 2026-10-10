package com.finalproject.oopfinalproject.Review;

import com.finalproject.oopfinalproject.Booking.Booking; // Assuming you have this
import com.finalproject.oopfinalproject.Booking.BookingManager; // Assuming you have this
import com.finalproject.oopfinalproject.User.Customer;
import com.finalproject.oopfinalproject.Vehicle.Vehicle;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewController {

    private final BookingManager bookingManager;

    public ReviewController(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    @PostMapping("/reviews/submit")
    public String handleReviewSubmission(@RequestParam String bookingID,
                                         @RequestParam int rating,
                                         @RequestParam String comment) {

        Booking booking = bookingManager.findBookingById(bookingID);

        Customer customer = booking.getCustomer();
        Vehicle vehicle = booking.getVehicle();

        String reviewID = "REV" + System.currentTimeMillis();

        Review newReview = new Review(reviewID, customer, vehicle, comment, rating);
        ReviewManager.saveReview(newReview);

        return "redirect:/review/review.html";
    }
}