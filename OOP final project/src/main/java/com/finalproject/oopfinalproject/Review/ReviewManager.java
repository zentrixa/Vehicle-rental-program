package com.finalproject.oopfinalproject.Review;

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
}
