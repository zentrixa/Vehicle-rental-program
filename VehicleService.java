package com.vehiclerental.vehicle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vehiclerental.common.FileHandler;
import com.vehiclerental.common.IdGenerator;
import com.vehiclerental.common.RequestData;
import com.vehiclerental.common.ResourceNotFoundException;
import com.vehiclerental.common.ValidationException;

/**
 * MEMBER 2 - Business logic + file handling for the fleet (vehicles.txt).
 *
 * Lecture concepts:
 *  - ArrayList&lt;Vehicle&gt; holds Car, Van and Motorcycle objects together (polymorphism through the parent type).
 *  - Overloaded constructors of Car/Van/Motorcycle are used when a new vehicle is added.
 *  - Open/Closed principle (SOLID): adding a new type (e.g. Truck) only needs a new child class and
 *    one line in build(); search, update and delete code stay unchanged.
 */
@Service
public class VehicleService {

    private static final String FILE = "vehicles.txt";
    private final ArrayList<Vehicle> vehicles = new ArrayList<>();

    public VehicleService() {
        for (String line : FileHandler.readLines(FILE)) {
            try {
                vehicles.add(Vehicle.fromFileString(line));
            } catch (RuntimeException e) {
                System.err.println("Skipping bad vehicle line: " + line);
            }
        }
    }

    // ------------------------------------------------------------------ CREATE

    public synchronized Vehicle add(RequestData data) {
        String plate = data.getString("plateNumber").toUpperCase().replace(' ', '-');
        ensurePlateUnique(null, plate);
        String id = IdGenerator.next("VEH", vehicles);
        Vehicle vehicle = build(data.getString("type").toUpperCase(), id, data, plate);
        vehicle.setImageUrl(data.getString("imageUrl", ""));
        vehicles.add(vehicle);
        save();
        return vehicle;
    }

    /** Creates the right child object with its overloaded constructor. */
    private Vehicle build(String type, String id, RequestData d, String plate) {
        String brand = d.getString("brand");
        String model = d.getString("model");
        int year = d.getInt("year");
        double rate = d.getDouble("dailyRate");
        int mileage = d.getInt("mileage", 0);
        String branch = d.getString("branchId", "");
        String colour = d.getString("colour", "White");
        String fuel = d.getString("fuelType", "Petrol");

        switch (type) {
            case Car.TYPE:
                return new Car(id, brand, model, year, plate, rate, mileage, branch, colour, fuel,
                        d.getInt("seats", 5), d.getString("transmission", "Automatic"));
            case Van.TYPE:
                return new Van(id, brand, model, year, plate, rate, mileage, branch, colour, fuel,
                        d.getInt("seats", 12), d.getInt("cargoCapacityKg", 800));
            case Motorcycle.TYPE:
                return new Motorcycle(id, brand, model, year, plate, rate, mileage, branch, colour, fuel,
                        d.getInt("engineCc", 125), d.getBoolean("helmetIncluded", true));
            default:
                throw new ValidationException("Vehicle type must be CAR, VAN or MOTORCYCLE");
        }
    }

    // ------------------------------------------------------------------ READ

    public synchronized List<Vehicle> getAll() {
        return new ArrayList<>(vehicles);
    }

    public synchronized Vehicle findById(String id) {
        for (Vehicle v : vehicles) {
            if (v.getId().equalsIgnoreCase(id)) {
                return v;
            }
        }
        throw new ResourceNotFoundException("Vehicle", id);
    }

    /**
     * Search by free text (brand, model, plate), type, status, branch and maximum daily rate.
     * Any filter that is null/empty is ignored.
     */
    public synchronized List<Vehicle> search(String q, String type, String status, String branchId, Double maxRate) {
        ArrayList<Vehicle> result = new ArrayList<>();
        String text = q == null ? "" : q.trim().toLowerCase();
        for (Vehicle v : vehicles) {
            if (!text.isEmpty()
                    && !(v.getBrand() + " " + v.getModel() + " " + v.getPlateNumber() + " " + v.getCategory())
                            .toLowerCase().contains(text)) {
                continue;
            }
            if (notEmpty(type) && !v.getType().equalsIgnoreCase(type)) {
                continue;
            }
            if (notEmpty(status) && !v.getStatus().name().equalsIgnoreCase(status)) {
                continue;
            }
            if (notEmpty(branchId) && !branchId.equalsIgnoreCase(v.getBranchId())) {
                continue;
            }
            if (maxRate != null && v.getDailyRate() > maxRate) {
                continue;
            }
            result.add(v);
        }
        return result;
    }

    /** Price quote - shows each type's own pricing rule (polymorphism). */
    public synchronized Map<String, Object> quote(String id, int days, double discountRate) {
        Vehicle v = findById(id);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("vehicleId", v.getId());
        map.put("days", days);
        map.put("standardCost", v.calculateRentalCost(days));
        map.put("discountRate", discountRate);
        map.put("finalCost", v.calculateRentalCost(days, discountRate));
        return map;
    }

    // ------------------------------------------------------------------ UPDATE

    public synchronized Vehicle update(String id, RequestData data) {
        Vehicle v = findById(id);
        if (data.has("plateNumber")) {
            String plate = data.getString("plateNumber").toUpperCase().replace(' ', '-');
            ensurePlateUnique(id, plate);
            v.setPlateNumber(plate);
        }
        v.setBrand(data.getString("brand", v.getBrand()));
        v.setModel(data.getString("model", v.getModel()));
        v.setYear(data.getInt("year", v.getYear()));
        v.setDailyRate(data.getDouble("dailyRate", v.getDailyRate()));
        int newMileage = data.getInt("mileage", v.getMileage());
        if (newMileage < v.getMileage()) {
            // correcting mileage is allowed, but only by an explicit flag
            if (!data.getBoolean("correctMileage", false)) {
                throw new ValidationException("New mileage is lower than the current reading. Tick 'correct mileage' to fix a mistake.");
            }
        }
        v.setMileage(newMileage);
        v.setBranchId(data.getString("branchId", v.getBranchId()));
        v.setColour(data.getString("colour", v.getColour()));
        v.setFuelType(data.getString("fuelType", v.getFuelType()));
        if (data.contains("imageUrl")) {                     // empty value = remove the photo
            v.setImageUrl(data.getString("imageUrl", ""));
        }
        if (data.has("status")) {
            v.setStatus(VehicleStatus.fromText(data.getString("status")));
        }
        v.applyTypeDetails(data);          // child class updates its own extra fields (polymorphism)
        save();
        return v;
    }

    /** Called by the booking module when a vehicle is rented / returned. */
    public synchronized Vehicle updateStatus(String id, VehicleStatus status) {
        Vehicle v = findById(id);
        v.setStatus(status);
        save();
        return v;
    }

    /** Adds kilometres driven during a rental (used when a vehicle is returned). */
    public synchronized void addMileage(String id, int km) {
        Vehicle v = findById(id);
        v.setMileage(v.getMileage() + Math.max(0, km));
        save();
    }

    // ------------------------------------------------------------------ DELETE

    public synchronized void delete(String id) {
        Vehicle v = findById(id);
        if (v.getStatus() == VehicleStatus.RENTED) {
            throw new ValidationException("This vehicle is on rent. Wait until it is returned before removing it.");
        }
        vehicles.remove(v);
        save();
    }

    // ------------------------------------------------------------------ statistics

    public synchronized Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        Map<String, Integer> byType = new LinkedHashMap<>();
        Map<String, Integer> byStatus = new LinkedHashMap<>();
        for (VehicleStatus s : VehicleStatus.values()) {
            byStatus.put(s.name(), 0);
        }
        for (Vehicle v : vehicles) {
            byType.merge(v.getType(), 1, Integer::sum);
            byStatus.merge(v.getStatus().name(), 1, Integer::sum);
        }
        stats.put("total", vehicles.size());
        stats.put("byType", byType);
        stats.put("byStatus", byStatus);
        return stats;
    }

    // ------------------------------------------------------------------ helpers

    private void ensurePlateUnique(String ignoreId, String plate) {
        for (Vehicle v : vehicles) {
            if (!v.getId().equals(ignoreId) && v.getPlateNumber().equalsIgnoreCase(plate)) {
                throw new ValidationException("Plate number " + plate + " already exists in the fleet");
            }
        }
    }

    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void save() {
        FileHandler.writeAll(FILE, vehicles);
    }
}
