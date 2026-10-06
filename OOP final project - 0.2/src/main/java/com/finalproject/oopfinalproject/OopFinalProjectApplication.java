package com.finalproject.oopfinalproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@SpringBootApplication
public class OopFinalProjectApplication {

     static void main(String[] args) {

        SpringApplication.run(OopFinalProjectApplication.class, args);

    }

    @GetMapping("/api/login")
    public void login(){

    }

    @GetMapping("/api/signup")
    public void signup(@RequestParam String name,
                       @RequestParam String password,
                       @RequestParam int contactNo,
                       @RequestParam String licenceNo,
                       @RequestParam String email) {

         System.out.print(name + password + contactNo + licenceNo + email);
         user Usr = new user();

    }



}
