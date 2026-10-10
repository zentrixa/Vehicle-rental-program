package com.finalproject.oopfinalproject.Vehicle;

public class Vehicle {
    protected String vehicleId;
    protected String brand;
    protected String model;
    protected String plateNumber;
    protected String category;
    protected double dailyRate;
    protected int mileage;
    protected boolean available;

    public Vehicle() {
        this.vehicleId = "";
        this.brand = "";
        this.model = "";
        this.plateNumber = "";
        this.category = "Standard";
        this.dailyRate = 0.0;
        this.mileage = 0;
        this.available = true;
    }

    public Vehicle(String vehicleId, String brand, String model, String plateNumber,
                   String category, double dailyRate, int mileage, boolean available) {
        this.vehicleId = "";
        this.brand = "";
        this.model = "";
        this.plateNumber = "";
        this.category = "";
        this.dailyRate = 0.0;
        this.mileage = -1;

        setVehicleId(vehicleId);
        setBrand(brand);
        setModel(model);
        setPlateNumber(plateNumber);
        setCategory(category);
        setDailyRate(dailyRate);
        setMileage(mileage);
        setAvailable(available);
    }

    public String getVehicleType() {
        return "Vehicle";
    }

    public double calculateRentalCost(int days) {
        return dailyRate * days;
    }

    public String getPricingRule() {
        return "Daily rate x days.";
    }

    public String getDisplayDetails() {
        return "";
    }

    protected String checkTypeDetails() {
        return "";
    }

    public double calculateRentalCost(int days, double discountPercent) {
        double cost = calculateRentalCost(days);
        return cost - (cost * discountPercent / 100);
    }

    public double getWeeklyCost() {
        return calculateRentalCost(7);
    }

    public boolean isValidRentalDays(int days) {
        if (days >= 1 && days <= 365) {
            return true;
        }
        return false;
    }

    public String checkDetails() {
        if (isEmpty(brand)) {
            return "Enter the brand (up to 40 characters, no | sign).";
        }
        if (isEmpty(model)) {
            return "Enter the model (up to 40 characters, no | sign).";
        }
        if (isEmpty(plateNumber)) {
            return "Enter the plate number with 4 to 12 letters, numbers, spaces or dashes, like CAB-1234.";
        }
        if (isEmpty(category)) {
            return "Choose a category: Economy, Standard or Luxury.";
        }
        if (dailyRate <= 0) {
            return "The daily rate must be more than 0 and at most 1,000,000.";
        }
        if (mileage < 0) {
            return "Enter the mileage as a whole number of km, from 0 to 2,000,000.";
        }
        return checkTypeDetails();
    }

    public String toFileString() {
        return baseFileString();
    }

    protected String baseFileString() {
        return vehicleId + "|" + getVehicleType() + "|" + brand + "|" + model + "|" + plateNumber + "|"
                + category + "|" + dailyRate + "|" + mileage + "|" + available;
    }

    protected boolean isValidText(String value, int maxLength) {
        if (value == null) {
            return false;
        }
        String text = value.trim();
        if (text.length() == 0 || text.length() > maxLength || text.contains("|")) {
            return false;
        }
        return true;
    }

    protected boolean isEmpty(String value) {
        if (value == null || value.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    void setVehicleId(String vehicleId) {
        if (vehicleId != null) {
            this.vehicleId = vehicleId.trim();
        }
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (isValidText(brand, 40)) {
            this.brand = brand.trim();
        } else {
            System.out.println("Brand must be 1 to 40 characters.");
        }
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        if (isValidText(model, 40)) {
            this.model = model.trim();
        } else {
            System.out.println("Model must be 1 to 40 characters.");
        }
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        if (!isValidText(plateNumber, 12)) {
            System.out.println("Plate number must be 4 to 12 characters.");
            return;
        }
        String text = plateNumber.trim().toUpperCase();
        boolean valid = text.length() >= 4;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean letter = c >= 'A' && c <= 'Z';
            boolean digit = c >= '0' && c <= '9';
            if (!letter && !digit && c != ' ' && c != '-') {
                valid = false;
            }
        }
        if (valid) {
            this.plateNumber = text;
        } else {
            System.out.println("Plate number can only have letters, numbers, spaces or dashes.");
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null) {
            System.out.println("Choose a category.");
            return;
        }
        String text = category.trim();
        if (text.equalsIgnoreCase("Economy")) {
            this.category = "Economy";
        } else if (text.equalsIgnoreCase("Standard")) {
            this.category = "Standard";
        } else if (text.equalsIgnoreCase("Luxury")) {
            this.category = "Luxury";
        } else {
            System.out.println("Category must be Economy, Standard or Luxury.");
        }
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        if (dailyRate > 0 && dailyRate <= 1000000) {
            this.dailyRate = dailyRate;
        } else {
            System.out.println("Daily rate must be more than 0 and at most 1,000,000.");
        }
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        if (mileage >= 0 && mileage <= 2000000) {
            this.mileage = mileage;
        } else {
            System.out.println("Mileage must be between 0 and 2,000,000 km.");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
