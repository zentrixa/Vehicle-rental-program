package com.finalproject.oopfinalproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RestController;


@RestController
@SpringBootApplication
public class OopFinalProjectApplication {
    private int userNum = 0;

     static void main(String[] args) {

        SpringApplication.run(OopFinalProjectApplication.class, args);

    }

    public void updateUserNum() {
         ++userNum;
    }

}
