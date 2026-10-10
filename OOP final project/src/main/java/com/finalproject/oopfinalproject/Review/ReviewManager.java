package com.finalproject.oopfinalproject.Review;

import com.finalproject.oopfinalproject.User.Customer;
import com.finalproject.oopfinalproject.User.RegularCustomer;
import com.finalproject.oopfinalproject.Vehicle.Vehicle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewManager {
    private static final String filePath="OOP final project/src/main/Database/reviews/reviews.txt";
    public static boolean saveReview(Review review){

        File file = new File(filePath);
        File folder=file.getParentFile();
        if(folder!=null && !folder.exists()){
            folder.mkdir();
        }

        try(BufferedWriter writer=new BufferedWriter(new FileWriter(file,true))){
            writer.write(review.toFileFormat());
            writer.newLine();
            return true;
        }
        catch(IOException e){
            System.err.println(e);
            return false;
        }

    }

    public static List<String> readAllReviews(){
        List<String> lines=new ArrayList<>();
        File file=new File(filePath);
        if(!file.exists()){
            return lines;
        }
        try(BufferedReader reader=new BufferedReader(new FileReader(file))){
            String line;
            while((line=reader.readLine())!=null){
                if(!line.trim().isEmpty()){
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return lines;

    }

    //update
    public static boolean updateReview(String targetReviewID, String newComment, int newRating) {
        List<String> lines = readAllReviews();
        boolean updated = false;
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length > 0 && parts[0].equals(targetReviewID)) {
                // Sanitize new comment
                String cleanComment = (newComment != null) ? newComment.replace(",", " ") : "";
                parts[3] = cleanComment;
                parts[4] = String.valueOf(newRating);

                line = String.join(",", parts);
                updated = true;
            }
            updatedLines.add(line);
        }

        if (updated) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
                for (String l : updatedLines) {
                    writer.write(l);
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return false;
    }

    //dlt
    public static boolean deleteReview(String targetReviewID) {
        List<String> lines = readAllReviews();
        boolean deleted = false;
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length > 0 && parts[0].equals(targetReviewID)) {
                deleted = true; // Skip adding this line to effectively delete it
                continue;
            }
            updatedLines.add(line);
        }

        if (deleted) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
                for (String l : updatedLines) {
                    writer.write(l);
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return false;
    }
    //Convert raw text lines into Review objects for display
    public static List<Review> getAllReviewObjects() {
        List<String> lines = readAllReviews();
        List<Review> reviewList = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 5) {
                String reviewID = parts[0];
                String customerID = parts[1];
                String vehicleID = parts[2];
                String comment = parts[3];
                int rating = Integer.parseInt(parts[4]);

                // Instantiate a concrete customer subclass using your teammate's constructor
                // (using placeholders for display purposes)
                Customer customer = new RegularCustomer(customerID, "dummyPass", "N/A", "N/A", "N/A");

                // For vehicle, use your team's concrete vehicle implementation or null if unneeded for display
                Vehicle vehicle = null;

                if (parts.length >= 7 && "VERIFIED".equals(parts[6])) {
                    VerifiedReview vReview = new VerifiedReview(reviewID, customer, vehicle, comment, rating, null);
                    reviewList.add(vReview);
                } else {
                    Review review = new Review(reviewID, customer, vehicle, comment, rating);
                    reviewList.add(review);
                }
            }
        }
        return reviewList;
    }
}
