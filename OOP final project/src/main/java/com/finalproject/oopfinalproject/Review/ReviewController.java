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

        Review newReview = new VerifiedReview(reviewID, customer, vehicle, comment, rating, booking);
        ReviewManager.saveReview(newReview);

        return "redirect:/review/review.html";
    }
    // Display all reviews on an HTML page via Thymeleaf
    @GetMapping("/reviews/list")
    public String listReviews(Model model) {
        model.addAttribute("reviews", ReviewManager.getAllReviewObjects());
        return "review-list"; // maps to review-list.html
    }

    // Handle delete action from HTML
    @PostMapping("/reviews/delete")
    public String deleteReview(@RequestParam String reviewID) {
        ReviewManager.deleteReview(reviewID);
        return "redirect:/reviews/list";
    }
}