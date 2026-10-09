package com.finalproject.oopfinalproject.review;

import java.io.File;

public class ReviewManager {
    private static final String filePath="OOP final project/src/main/Database/reviews/reviews.txt";
    public static boolean saveReview(Review review){

        File file = new File(filePath);
        File folder=file.getParentFile();
        if(folder!=null && !folder.exists()){
            folder.mkdir();
        }


    }
}
