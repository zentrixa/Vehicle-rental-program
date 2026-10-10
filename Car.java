package com.vehiclerental.vehicle;

import java.util.ArrayList;
import java.util.List;

import com.vehiclerental.common.RequestData;
import com.vehiclerental.common.ValidationException;

/**
 * MEMBER 2 - A car (sedan, hatchback, SUV ...).
 * Pricing rule: daily rate x days, with 10 % off for rentals of 7 days or more.
 */
public class Car extends Vehicle {

    public static final String TYPE = "CAR";
    private static final double WEEKLY_DISCOUNT = 0.10;

    private int seats;
    private String transmission;   // Automatic / Manual

    public Car() {
        super();
        this.seats = 5;
        this.transmission = "Automatic";
    }

    public Car(String id, String brand, String model, int year, String plateNumber, double dailyRate,
               int mileage, String branchId, String colour, String fuelType, int seats, String transmission) {
        super(id, brand, model, year, plateNumber, dailyRate, mileage, branchId, colour, fuelType);
        setSeats(seats);
        setTransmission(transmission);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getCategory() {
        return seats > 5 ? "SUV / 7 seater" : "Sedan / Hatchback";
    }

    @Override
    public double calculateRentalCost(int days) {
        checkDays(days);
        double cost = getDailyRate() * days;
        if (days >= 7) {
            cost = cost * (1 - WEEKLY_DISCOUNT);
        }
        return Math.round(cost * 100.0) / 100.0;
    }

    @Override
    public List<String> getFeatures() {
        List<String> list = new ArrayList<>();
        list.add(seats + " seats");
        list.add(transmission);
        list.add(getFuelType());
        list.add("10% off for 7+ days");
        return list;
    }

    @Override
    protected String[] extraFileFields() {
        return new String[] { String.valueOf(seats), transmission };
    }

    @Override
    protected void readExtraFields(String extra1, String extra2) {
        this.seats = Integer.parseInt(extra1);
        this.transmission = extra2;
    }

    @Override
    public void applyTypeDetails(RequestData data) {
        setSeats(data.getInt("seats", seats));
        setTransmission(data.getString("transmission", transmission));
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        if (seats < 2 || seats > 9) {
            throw new ValidationException("A car must have between 2 and 9 seats");
        }
        this.seats = seats;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = "Manual".equalsIgnoreCase(transmission) ? "Manual" : "Automatic";
    }
}
