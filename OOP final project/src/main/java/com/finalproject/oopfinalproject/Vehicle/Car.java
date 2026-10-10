package com.finalproject.oopfinalproject.Vehicle;

public class Car extends Vehicle {
    public static final int LONG_RENTAL_DAYS = 7;
    public static final double LONG_RENTAL_DISCOUNT = 0.10;
    public static final String[] FUEL_TYPES = {"Petrol", "Diesel", "Hybrid", "Electric"};

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
        setSeats(seats);
        setFuelType(fuelType);
    }

    @Override
    public String getVehicleType() {
        return "Car";
    }

    @Override
    public double calculateRentalCost(int days) {
        checkRentalDays(days);
        double cost = getDailyRate() * days;
        if (days >= LONG_RENTAL_DAYS) {
            cost = cost - (cost * LONG_RENTAL_DISCOUNT);
        }
        return cost;
    }

    @Override
    public String getPricingRule() {
        return "Daily rate x days, with 10% off rentals of 7 days or more.";
    }

    @Override
    public String getDisplayDetails() {
        return seats + " seats, " + fuelType;
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + seats + "|" + fuelType;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        if (seats < 2 || seats > 9) {
            throw new IllegalArgumentException("A car must have 2 to 9 seats.");
        }
        this.seats = seats;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        String text = checkText(fuelType, "fuel type", 20);
        for (String allowed : FUEL_TYPES) {
            if (allowed.equalsIgnoreCase(text)) {
                this.fuelType = allowed;
                return;
            }
        }
        throw new IllegalArgumentException("Choose a fuel type: Petrol, Diesel, Hybrid or Electric.");
    }
}
