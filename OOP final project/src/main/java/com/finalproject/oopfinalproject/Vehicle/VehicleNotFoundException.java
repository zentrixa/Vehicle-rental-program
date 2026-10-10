package com.finalproject.oopfinalproject.Vehicle;

public class VehicleNotFoundException extends Exception {
    public VehicleNotFoundException(String vehicleId) {
        super("No vehicle has the ID " + vehicleId + ". It may have been deleted.");
    }
}
