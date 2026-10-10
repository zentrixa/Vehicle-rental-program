package com.finalproject.oopfinalproject.Vehicle;

public class Car extends Vehicle {
    private int seats;
    private String fuelType;

    public Car() {
        super();
        this.seats = 4;
        this.fuelType = "Petrol";
    }

    public Car(String vehicleId, String brand, String model, String plateNumber, String category,
               double dailyRate, int mileage, boolean available, int seats, String fuelType) {
        super(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available);
        this.seats = 0;
        this.fuelType = "";
        setSeats(seats);
        setFuelType(fuelType);
    }

    public String getVehicleType() {
        return "Car";
    }

    public double calculateRentalCost(int days) {
        double cost = dailyRate * days;
        if (days >= 7) {
            cost = cost - (cost * 10 / 100);
        }
        return cost;
    }

    public String getPricingRule() {
        return "Daily rate x days, with 10% off rentals of 7 days or more.";
    }

    public String getDisplayDetails() {
        return seats + " seats, " + fuelType;
    }

    protected String checkTypeDetails() {
        if (seats == 0) {
            return "A car must have 2 to 9 seats.";
        }
        if (isEmpty(fuelType)) {
            return "Choose a fuel type: Petrol, Diesel, Hybrid or Electric.";
        }
        return "";
    }

    public String toFileString() {
        return baseFileString() + "|" + seats + "|" + fuelType;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        if (seats >= 2 && seats <= 9) {
            this.seats = seats;
        } else {
            System.out.println("A car must have 2 to 9 seats.");
        }
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        if (fuelType == null) {
            System.out.println("Choose a fuel type.");
            return;
        }
        String text = fuelType.trim();
        if (text.equalsIgnoreCase("Petrol")) {
            this.fuelType = "Petrol";
        } else if (text.equalsIgnoreCase("Diesel")) {
            this.fuelType = "Diesel";
        } else if (text.equalsIgnoreCase("Hybrid")) {
            this.fuelType = "Hybrid";
        } else if (text.equalsIgnoreCase("Electric")) {
            this.fuelType = "Electric";
        } else {
            System.out.println("Fuel type must be Petrol, Diesel, Hybrid or Electric.");
        }
    }
}
