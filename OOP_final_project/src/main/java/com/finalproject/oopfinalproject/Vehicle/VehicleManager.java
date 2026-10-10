package com.finalproject.oopfinalproject.Vehicle;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

@Component
public class VehicleManager {
    private String filePath = "src/main/Database/vehicles.txt";

    public Vehicle createVehicle(String type, String vehicleId, String brand, String model,
                                 String plateNumber, String category, double dailyRate, int mileage,
                                 boolean available, int seats, String fuelType, int cargoCapacity,
                                 int engineCc) {
        if (type == null) {
            return null;
        }
        if (type.equalsIgnoreCase("Car")) {
            return new Car(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available,
                    seats, fuelType);
        }
        if (type.equalsIgnoreCase("Van")) {
            return new Van(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available,
                    cargoCapacity);
        }
        if (type.equalsIgnoreCase("Motorcycle")) {
            return new Motorcycle(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available,
                    engineCc);
        }
        return null;
    }

    public boolean addVehicle(Vehicle vehicle) {
        Vehicle[] vehicles = getAllVehicles();
        vehicle.setVehicleId(nextVehicleId(vehicles));

        Vehicle[] updated = new Vehicle[vehicles.length + 1];
        for (int i = 0; i < vehicles.length; i++) {
            updated[i] = vehicles[i];
        }
        updated[vehicles.length] = vehicle;

        return saveVehicles(updated);
    }

    public Vehicle[] getAllVehicles() {
        return loadVehicles();
    }

    public Vehicle findVehicleById(String vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        Vehicle[] vehicles = getAllVehicles();
        for (int i = 0; i < vehicles.length; i++) {
            if (vehicles[i].getVehicleId().equalsIgnoreCase(vehicleId.trim())) {
                return vehicles[i];
            }
        }
        return null;
    }

    public Vehicle[] searchVehicles(String brand, String type, String category) {
        Vehicle[] vehicles = getAllVehicles();
        String brandText = brand.trim().toLowerCase();

        int matches = 0;
        for (int i = 0; i < vehicles.length; i++) {
            if (matchesSearch(vehicles[i], brandText, type.trim(), category.trim())) {
                matches++;
            }
        }

        Vehicle[] results = new Vehicle[matches];
        int position = 0;
        for (int i = 0; i < vehicles.length; i++) {
            if (matchesSearch(vehicles[i], brandText, type.trim(), category.trim())) {
                results[position] = vehicles[i];
                position++;
            }
        }
        return results;
    }

    public boolean isPlateNumberTaken(String plateNumber, String ignoreVehicleId) {
        Vehicle[] vehicles = getAllVehicles();
        for (int i = 0; i < vehicles.length; i++) {
            boolean samePlate = vehicles[i].getPlateNumber().equalsIgnoreCase(plateNumber);
            boolean sameVehicle = vehicles[i].getVehicleId().equalsIgnoreCase(ignoreVehicleId);
            if (samePlate && !sameVehicle) {
                return true;
            }
        }
        return false;
    }

    public boolean updateVehicle(Vehicle vehicle) {
        Vehicle[] vehicles = getAllVehicles();
        for (int i = 0; i < vehicles.length; i++) {
            if (vehicles[i].getVehicleId().equalsIgnoreCase(vehicle.getVehicleId())) {
                vehicles[i] = vehicle;
                return saveVehicles(vehicles);
            }
        }
        return false;
    }

    public boolean deleteVehicle(String vehicleId) {
        Vehicle[] vehicles = getAllVehicles();
        int position = -1;
        for (int i = 0; i < vehicles.length; i++) {
            if (vehicles[i].getVehicleId().equalsIgnoreCase(vehicleId)) {
                position = i;
            }
        }
        if (position == -1) {
            return false;
        }

        Vehicle[] updated = new Vehicle[vehicles.length - 1];
        int next = 0;
        for (int i = 0; i < vehicles.length; i++) {
            if (i != position) {
                updated[next] = vehicles[i];
                next++;
            }
        }
        return saveVehicles(updated);
    }

    private Vehicle[] loadVehicles() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new Vehicle[0];
        }

        try {
            int lineCount = 0;
            Scanner counter = new Scanner(file);
            while (counter.hasNextLine()) {
                if (counter.nextLine().trim().length() > 0) {
                    lineCount++;
                }
            }
            counter.close();

            Vehicle[] vehicles = new Vehicle[lineCount];
            int count = 0;
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine() && count < lineCount) {
                String line = reader.nextLine();
                if (line.trim().length() > 0) {
                    Vehicle vehicle = readVehicle(line);
                    if (vehicle != null) {
                        vehicles[count] = vehicle;
                        count++;
                    }
                }
            }
            reader.close();

            if (count < lineCount) {
                Vehicle[] valid = new Vehicle[count];
                for (int i = 0; i < count; i++) {
                    valid[i] = vehicles[i];
                }
                return valid;
            }
            return vehicles;
        } catch (FileNotFoundException e) {
            System.out.println("Could not read vehicles.txt: " + e.getMessage());
            return new Vehicle[0];
        }
    }

    private Vehicle readVehicle(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 10 || !isNumber(parts[6]) || !isNumber(parts[7]) || !isNumber(parts[9])) {
            System.out.println("Skipping a damaged line in vehicles.txt: " + line);
            return null;
        }

        String type = parts[1];
        double dailyRate = Double.parseDouble(parts[6]);
        int mileage = Integer.parseInt(parts[7]);
        boolean available = parts[8].equalsIgnoreCase("true");

        int seats = 0;
        String fuelType = "";
        int cargoCapacity = 0;
        int engineCc = 0;
        if (type.equalsIgnoreCase("Car") && parts.length >= 11) {
            seats = Integer.parseInt(parts[9]);
            fuelType = parts[10];
        } else if (type.equalsIgnoreCase("Van")) {
            cargoCapacity = Integer.parseInt(parts[9]);
        } else if (type.equalsIgnoreCase("Motorcycle")) {
            engineCc = Integer.parseInt(parts[9]);
        }

        Vehicle vehicle = createVehicle(type, parts[0], parts[2], parts[3], parts[4], parts[5],
                dailyRate, mileage, available, seats, fuelType, cargoCapacity, engineCc);
        if (vehicle == null || !vehicle.checkDetails().equals("")) {
            System.out.println("Skipping a damaged line in vehicles.txt: " + line);
            return null;
        }
        return vehicle;
    }

    private boolean isNumber(String text) {
        if (text.length() == 0 || text.length() > 15) {
            return false;
        }
        int dots = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.') {
                dots++;
            } else if (c < '0' || c > '9') {
                return false;
            }
        }
        return dots <= 1;
    }

    private boolean saveVehicles(Vehicle[] vehicles) {
        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try {
            PrintWriter writer = new PrintWriter(file);
            for (int i = 0; i < vehicles.length; i++) {
                writer.println(vehicles[i].toFileString());
            }
            writer.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("Could not save vehicles.txt: " + e.getMessage());
            return false;
        }
    }

    private boolean matchesSearch(Vehicle vehicle, String brandText, String type, String category) {
        boolean brandMatches = brandText.length() == 0
                || vehicle.getBrand().toLowerCase().contains(brandText)
                || vehicle.getModel().toLowerCase().contains(brandText);
        boolean typeMatches = type.length() == 0 || vehicle.getVehicleType().equalsIgnoreCase(type);
        boolean categoryMatches = category.length() == 0 || vehicle.getCategory().equalsIgnoreCase(category);
        return brandMatches && typeMatches && categoryMatches;
    }

    private String nextVehicleId(Vehicle[] vehicles) {
        int highest = 0;
        for (int i = 0; i < vehicles.length; i++) {
            String id = vehicles[i].getVehicleId();
            int number = 0;

            for (int c = 1; c < id.length(); c++) {
                char digit = id.charAt(c);
                if (digit >= '0' && digit <= '9') {
                    number = number * 10 + (digit - '0');
                }
            }
            if (number > highest) {
                highest = number;
            }
        }

        int next = highest + 1;
        if (next < 10) {
            return "V00" + next;
        } else if (next < 100) {
            return "V0" + next;
        }
        return "V" + next;
    }
}
