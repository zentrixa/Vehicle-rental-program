package com.finalproject.oopfinalproject.Vehicle;

public class DuplicatePlateNumberException extends Exception {
    public DuplicatePlateNumberException(String plateNumber) {
        super("Another vehicle already has the plate number " + plateNumber + ".");
    }
}
