package com.finalproject.oopfinalproject.Vehicle;

public class Motorcycle extends Vehicle {
    public static final double HELMET_FEE_PER_DAY = 100.0;

    private int engineCc;

    public Motorcycle() {
        super();
        this.engineCc = 125;
    }

    public Motorcycle(String vehicleId, String brand, String model, String plateNumber, String category,
                      double dailyRate, int mileage, boolean available, int engineCc) {
        super(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available);
        setEngineCc(engineCc);
    }

    @Override
    public String getVehicleType() {
        return "Motorcycle";
    }

    @Override
    public double calculateRentalCost(int days) {
        checkRentalDays(days);
        return (getDailyRate() + HELMET_FEE_PER_DAY) * days;
    }

    @Override
    public String getPricingRule() {
        return "Daily rate x days, plus Rs. 100 a day for helmet hire.";
    }

    @Override
    public String getDisplayDetails() {
        return engineCc + " cc engine";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + engineCc;
    }

    public int getEngineCc() {
        return engineCc;
    }

    public void setEngineCc(int engineCc) {
        if (engineCc < 50 || engineCc > 2000) {
            throw new IllegalArgumentException("Engine size must be between 50 and 2000 cc.");
        }
        this.engineCc = engineCc;
    }
}
