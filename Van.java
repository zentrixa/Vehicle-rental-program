package com.vehiclerental.vehicle;

import java.util.ArrayList;
import java.util.List;

import com.vehiclerental.common.RequestData;
import com.vehiclerental.common.ValidationException;

/**
 * MEMBER 2 - A van for groups or cargo.
 * Pricing rule: daily rate x days + a one-time cleaning fee of Rs. 2,000.
 */
public class Van extends Vehicle {

    public static final String TYPE = "VAN";
    public static final double CLEANING_FEE = 2000.0;

    private int seats;
    private int cargoCapacityKg;

    public Van() {
        super();
        this.seats = 12;
        this.cargoCapacityKg = 800;
    }

    public Van(String id, String brand, String model, int year, String plateNumber, double dailyRate,
               int mileage, String branchId, String colour, String fuelType, int seats, int cargoCapacityKg) {
        super(id, brand, model, year, plateNumber, dailyRate, mileage, branchId, colour, fuelType);
        setSeats(seats);
        setCargoCapacityKg(cargoCapacityKg);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getCategory() {
        if (seats >= 10) {
            return "Passenger van";
        }
        return seats >= 6 ? "Family van / MPV" : "Cargo van";
    }

    @Override
    public double calculateRentalCost(int days) {
        checkDays(days);
        return Math.round((getDailyRate() * days + CLEANING_FEE) * 100.0) / 100.0;
    }

    @Override
    public List<String> getFeatures() {
        List<String> list = new ArrayList<>();
        list.add(seats + " seats");
        list.add(cargoCapacityKg + " kg cargo");
        list.add(getFuelType());
        list.add("Rs. 2,000 cleaning fee");
        return list;
    }

    @Override
    protected String[] extraFileFields() {
        return new String[] { String.valueOf(seats), String.valueOf(cargoCapacityKg) };
    }

    @Override
    protected void readExtraFields(String extra1, String extra2) {
        this.seats = Integer.parseInt(extra1);
        this.cargoCapacityKg = Integer.parseInt(extra2);
    }

    @Override
    public void applyTypeDetails(RequestData data) {
        setSeats(data.getInt("seats", seats));
        setCargoCapacityKg(data.getInt("cargoCapacityKg", cargoCapacityKg));
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        if (seats < 2 || seats > 30) {
            throw new ValidationException("A van must have between 2 and 30 seats");
        }
        this.seats = seats;
    }

    public int getCargoCapacityKg() {
        return cargoCapacityKg;
    }

    public void setCargoCapacityKg(int cargoCapacityKg) {
        if (cargoCapacityKg < 0) {
            throw new ValidationException("Cargo capacity cannot be negative");
        }
        this.cargoCapacityKg = cargoCapacityKg;
    }
}
