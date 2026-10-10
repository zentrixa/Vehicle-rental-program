package com.finalproject.oopfinalproject.Vehicle;

public class Motorcycle extends Vehicle {
    private int engineCc;

    public Motorcycle() {
        super();
        this.engineCc = 125;
    }

    public Motorcycle(String vehicleId, String brand, String model, String plateNumber, String category,
                      double dailyRate, int mileage, boolean available, int engineCc) {
        super(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available);
        this.engineCc = 0;
        setEngineCc(engineCc);
    }

    public String getVehicleType() {
        return "Motorcycle";
    }

    public double calculateRentalCost(int days) {
        return (dailyRate + 100) * days;
    }

    public String getPricingRule() {
        return "Daily rate x days, plus Rs. 100 a day for helmet hire.";
    }

    public String getDisplayDetails() {
        return engineCc + " cc engine";
    }

    protected String checkTypeDetails() {
        if (engineCc == 0) {
            return "Engine size must be between 50 and 2000 cc.";
        }
        return "";
    }

    public String toFileString() {
        return baseFileString() + "|" + engineCc;
    }

    public int getEngineCc() {
        return engineCc;
    }

    public void setEngineCc(int engineCc) {
        if (engineCc >= 50 && engineCc <= 2000) {
            this.engineCc = engineCc;
        } else {
            System.out.println("Engine size must be between 50 and 2000 cc.");
        }
    }
}
