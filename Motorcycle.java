package com.vehiclerental.vehicle;

import java.util.ArrayList;
import java.util.List;

import com.vehiclerental.common.RequestData;
import com.vehiclerental.common.ValidationException;

/**
 * MEMBER 2 - A motorcycle or scooter.
 * Pricing rule: daily rate x days, plus Rs. 150 per day if a helmet is not included,
 * with 5 % off for 3 days or more.
 */
public class Motorcycle extends Vehicle {

    public static final String TYPE = "MOTORCYCLE";
    private static final double HELMET_FEE_PER_DAY = 150.0;

    private int engineCc;
    private boolean helmetIncluded;

    public Motorcycle() {
        super();
        this.engineCc = 125;
        this.helmetIncluded = true;
    }

    public Motorcycle(String id, String brand, String model, int year, String plateNumber, double dailyRate,
                      int mileage, String branchId, String colour, String fuelType, int engineCc, boolean helmetIncluded) {
        super(id, brand, model, year, plateNumber, dailyRate, mileage, branchId, colour, fuelType);
        setEngineCc(engineCc);
        this.helmetIncluded = helmetIncluded;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getCategory() {
        return engineCc <= 150 ? "Scooter / Commuter" : "Touring bike";
    }

    @Override
    public double calculateRentalCost(int days) {
        checkDays(days);
        double cost = getDailyRate() * days;
        if (!helmetIncluded) {
            cost += HELMET_FEE_PER_DAY * days;
        }
        if (days >= 3) {
            cost = cost * 0.95;
        }
        return Math.round(cost * 100.0) / 100.0;
    }

    @Override
    public List<String> getFeatures() {
        List<String> list = new ArrayList<>();
        list.add(engineCc + " cc");
        list.add(helmetIncluded ? "Helmet included" : "Helmet Rs. 150/day");
        list.add(getFuelType());
        list.add("5% off for 3+ days");
        return list;
    }

    @Override
    protected String[] extraFileFields() {
        return new String[] { String.valueOf(engineCc), String.valueOf(helmetIncluded) };
    }

    @Override
    protected void readExtraFields(String extra1, String extra2) {
        this.engineCc = Integer.parseInt(extra1);
        this.helmetIncluded = Boolean.parseBoolean(extra2);
    }

    @Override
    public void applyTypeDetails(RequestData data) {
        setEngineCc(data.getInt("engineCc", engineCc));
        setHelmetIncluded(data.getBoolean("helmetIncluded", helmetIncluded));
    }

    public int getEngineCc() {
        return engineCc;
    }

    public void setEngineCc(int engineCc) {
        if (engineCc < 50 || engineCc > 1500) {
            throw new ValidationException("Engine capacity must be between 50 and 1500 cc");
        }
        this.engineCc = engineCc;
    }

    public boolean isHelmetIncluded() {
        return helmetIncluded;
    }

    public void setHelmetIncluded(boolean helmetIncluded) {
        this.helmetIncluded = helmetIncluded;
    }
}
