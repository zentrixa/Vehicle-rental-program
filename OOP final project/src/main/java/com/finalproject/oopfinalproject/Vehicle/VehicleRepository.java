package com.finalproject.oopfinalproject.Vehicle;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class VehicleRepository {
    private static final String FILE_PATH = "src/main/Database/vehicles.txt";

    public void addVehicle(Vehicle vehicle) throws IOException, DuplicatePlateNumberException {
        ArrayList<Vehicle> vehicles = getAllVehicles();

        for (Vehicle existing : vehicles) {
            if (existing.getPlateNumber().equalsIgnoreCase(vehicle.getPlateNumber())) {
                throw new DuplicatePlateNumberException(vehicle.getPlateNumber());
            }
        }

        vehicle.setVehicleId(nextId(vehicles));
        vehicles.add(vehicle);
        saveAll(vehicles);
    }

    public ArrayList<Vehicle> getAllVehicles() throws IOException {
        ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return vehicles;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    Vehicle vehicle = VehicleFactory.fromFileString(line);
                    if (vehicle != null) {
                        vehicles.add(vehicle);
                    }
                }
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return vehicles;
    }

    public Vehicle findById(String vehicleId) throws IOException, VehicleNotFoundException {
        for (Vehicle vehicle : getAllVehicles()) {
            if (vehicle.getVehicleId().equalsIgnoreCase(vehicleId)) {
                return vehicle;
            }
        }
        throw new VehicleNotFoundException(vehicleId);
    }

    public ArrayList<Vehicle> searchVehicles(String brand, String type, String category) throws IOException {
        ArrayList<Vehicle> results = new ArrayList<Vehicle>();
        String brandText = brand.trim().toLowerCase();
        String typeText = type.trim();
        String categoryText = category.trim();

        for (Vehicle vehicle : getAllVehicles()) {
            boolean brandMatches = brandText.isEmpty()
                    || vehicle.getBrand().toLowerCase().contains(brandText)
                    || vehicle.getModel().toLowerCase().contains(brandText);
            boolean typeMatches = typeText.isEmpty()
                    || vehicle.getVehicleType().equalsIgnoreCase(typeText);
            boolean categoryMatches = categoryText.isEmpty()
                    || vehicle.getCategory().equalsIgnoreCase(categoryText);

            if (brandMatches && typeMatches && categoryMatches) {
                results.add(vehicle);
            }
        }
        return results;
    }

    public void updateVehicle(Vehicle updated)
            throws IOException, VehicleNotFoundException, DuplicatePlateNumberException {
        ArrayList<Vehicle> vehicles = getAllVehicles();
        int position = -1;

        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle current = vehicles.get(i);
            if (current.getVehicleId().equalsIgnoreCase(updated.getVehicleId())) {
                position = i;
            } else if (current.getPlateNumber().equalsIgnoreCase(updated.getPlateNumber())) {
                throw new DuplicatePlateNumberException(updated.getPlateNumber());
            }
        }

        if (position == -1) {
            throw new VehicleNotFoundException(updated.getVehicleId());
        }

        vehicles.set(position, updated);
        saveAll(vehicles);
    }

    public Vehicle deleteVehicle(String vehicleId) throws IOException, VehicleNotFoundException {
        ArrayList<Vehicle> vehicles = getAllVehicles();

        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getVehicleId().equalsIgnoreCase(vehicleId)) {
                Vehicle removed = vehicles.remove(i);
                saveAll(vehicles);
                return removed;
            }
        }
        throw new VehicleNotFoundException(vehicleId);
    }

    private void saveAll(ArrayList<Vehicle> vehicles) throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (Vehicle vehicle : vehicles) {
                writer.write(vehicle.toFileString());
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String nextId(ArrayList<Vehicle> vehicles) {
        int highest = 0;
        for (Vehicle vehicle : vehicles) {
            try {
                int number = Integer.parseInt(vehicle.getVehicleId().substring(1));
                if (number > highest) {
                    highest = number;
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            }
        }
        return String.format("V%03d", highest + 1);
    }
}
