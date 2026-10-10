package com.finalproject.oopfinalproject.Vehicle;

public class Van extends Vehicle {
    private int cargoCapacity;

    public Van() {
        super();
        this.cargoCapacity = 1000;
    }

    public Van(String vehicleId, String brand, String model, String plateNumber, String category,
               double dailyRate, int mileage, boolean available, int cargoCapacity) {
        super(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available);
        this.cargoCapacity = 0;
        setCargoCapacity(cargoCapacity);
    }

    public String getVehicleType() {
        return "Van";
    }

    public double calculateRentalCost(int days) {
        return (dailyRate * days) + 2000;
    }

    public String getPricingRule() {
        return "Daily rate x days, plus a Rs. 2,000 cleaning fee per rental.";
    }

    public String getDisplayDetails() {
        return "Carries up to " + cargoCapacity + " kg";
    }

    protected String checkTypeDetails() {
        if (cargoCapacity == 0) {
            return "Cargo capacity must be between 100 and 5000 kg.";
        }
        return "";
    }

    public String toFileString() {
        return baseFileString() + "|" + cargoCapacity;
    }

    public int getCargoCapacity() {
        return cargoCapacity;
    }

    public void setCargoCapacity(int cargoCapacity) {
        if (cargoCapacity >= 100 && cargoCapacity <= 5000) {
            this.cargoCapacity = cargoCapacity;
        } else {
            System.out.println("Cargo capacity must be between 100 and 5000 kg.");
        }
    }
}
