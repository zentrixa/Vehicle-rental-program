package com.finalproject.oopfinalproject.Vehicle;

public abstract class Vehicle implements Rentable {
    public static final String[] CATEGORIES = {"Economy", "Standard", "Luxury"};
    public static final int MIN_RENTAL_DAYS = 1;
    public static final int MAX_RENTAL_DAYS = 365;

    private String vehicleId;
    private String brand;
    private String model;
    private String plateNumber;
    private String category;
    private double dailyRate;
    private int mileage;
    private boolean available;

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
        setVehicleId(vehicleId);
        setBrand(brand);
        setModel(model);
        setPlateNumber(plateNumber);
        setCategory(category);
        setDailyRate(dailyRate);
        setMileage(mileage);
        setAvailable(available);
    }

    public abstract String getVehicleType();

    public abstract String getDisplayDetails();

    public double calculateRentalCost(int days, double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100 percent.");
        }
        double cost = calculateRentalCost(days);
        return cost - (cost * discountPercent / 100);
    }

    public double getWeeklyCost() {
        return calculateRentalCost(7);
    }

    protected void checkRentalDays(int days) {
        if (days < MIN_RENTAL_DAYS || days > MAX_RENTAL_DAYS) {
            throw new IllegalArgumentException("Rental days must be between "
                    + MIN_RENTAL_DAYS + " and " + MAX_RENTAL_DAYS + ".");
        }
    }

    public String toFileString() {
        return vehicleId + "|" + getVehicleType() + "|" + brand + "|" + model + "|"
                + plateNumber + "|" + category + "|" + dailyRate + "|" + mileage + "|" + available;
    }

    protected static String checkText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter the " + fieldName + ".");
        }
        String text = value.trim();
        if (text.length() > maxLength) {
            throw new IllegalArgumentException("The " + fieldName + " can be at most " + maxLength + " characters.");
        }

        if (text.contains("|")) {
            throw new IllegalArgumentException("The " + fieldName + " can't contain the | character.");
        }
        return text;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    void setVehicleId(String vehicleId) {
        if (vehicleId == null) {
            this.vehicleId = "";
        } else {
            this.vehicleId = vehicleId.trim();
        }
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = checkText(brand, "brand", 40);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = checkText(model, "model", 40);
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        String text = checkText(plateNumber, "plate number", 12).toUpperCase();
        if (!text.matches("[A-Z0-9 -]{4,12}")) {
            throw new IllegalArgumentException(
                    "Enter the plate number with 4 to 12 letters, numbers, spaces or dashes, like CAB-1234.");
        }
        this.plateNumber = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        String text = checkText(category, "category", 20);
        for (String allowed : CATEGORIES) {
            if (allowed.equalsIgnoreCase(text)) {
                this.category = allowed;
                return;
            }
        }
        throw new IllegalArgumentException("Choose a category: Economy, Standard or Luxury.");
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        if (dailyRate <= 0 || dailyRate > 1000000) {
            throw new IllegalArgumentException("The daily rate must be more than 0 and at most 1,000,000.");
        }
        this.dailyRate = dailyRate;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        if (mileage < 0 || mileage > 2000000) {
            throw new IllegalArgumentException("Mileage must be between 0 and 2,000,000 km.");
        }
        this.mileage = mileage;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
