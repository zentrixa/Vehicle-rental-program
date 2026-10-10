package com.finalproject.oopfinalproject.Vehicle;

public class Van extends Vehicle {
    public static final double CLEANING_FEE = 2000.0;

    private int cargoCapacity;

    public Van() {
        super();
        this.cargoCapacity = 1000;
    }

    public Van(String vehicleId, String brand, String model, String plateNumber, String category,
               double dailyRate, int mileage, boolean available, int cargoCapacity) {
        super(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available);
        setCargoCapacity(cargoCapacity);
    }

    @Override
    public String getVehicleType() {
        return "Van";
    }

    @Override
    public double calculateRentalCost(int days) {
        checkRentalDays(days);
        return (getDailyRate() * days) + CLEANING_FEE;
    }

    @Override
    public String getPricingRule() {
        return "Daily rate x days, plus a Rs. 2,000 cleaning fee per rental.";
    }

    @Override
    public String getDisplayDetails() {
        return "Carries up to " + cargoCapacity + " kg";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + cargoCapacity;
    }

    public int getCargoCapacity() {
        return cargoCapacity;
    }

    public void setCargoCapacity(int cargoCapacity) {
        if (cargoCapacity < 100 || cargoCapacity > 5000) {
            throw new IllegalArgumentException("Cargo capacity must be between 100 and 5000 kg.");
        }
        this.cargoCapacity = cargoCapacity;
    }
}
