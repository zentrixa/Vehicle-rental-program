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
            System.out.println("vehicles.txt was not found at " + file.getAbsolutePath()
                    + ". Creating it with the starter vehicles.");
            if (!saveStarterVehicles()) {
                return new Vehicle[0];
            }
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
        int imagePosition = 10;
        if (type.equalsIgnoreCase("Car") && parts.length >= 11) {
            seats = Integer.parseInt(parts[9]);
            fuelType = parts[10];
            imagePosition = 11;
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
        if (parts.length > imagePosition) {
            vehicle.setImageName(parts[imagePosition]);
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

    private boolean saveStarterVehicles() {
        String[] lines = getStarterLines();
        Vehicle[] starter = new Vehicle[lines.length];
        for (int i = 0; i < lines.length; i++) {
            starter[i] = readVehicle(lines[i]);
        }
        return saveVehicles(starter);
    }

    private String[] getStarterLines() {
        String[] lines = {
            "V001|Car|Toyota|Axio|CAB-1234|Standard|6500.0|45000|true|5|Petrol|cars/toyota-axio.jpg",
            "V002|Car|Toyota|Prius|CAD-5678|Luxury|9500.0|30000|true|5|Hybrid|cars/toyota-prius.jpg",
            "V003|Car|Suzuki|Swift|CAE-2468|Economy|4500.0|38000|true|5|Petrol|cars/suzuki-swift.jpg",
            "V004|Car|Honda|Civic|CAF-3141|Luxury|11000.0|22000|true|5|Petrol|cars/honda-civic.jpg",
            "V005|Car|Honda|Vezel|CAG-7720|Standard|8500.0|41000|true|5|Hybrid|cars/honda-vezel.jpg",
            "V006|Car|Mazda|3|CAH-5590|Standard|8000.0|36000|true|5|Petrol|cars/mazda-3.jpg",
            "V007|Car|Mitsubishi|Outlander|CAJ-8812|Luxury|12500.0|28000|true|7|Hybrid|cars/mitsubishi-outlander.jpg",
            "V008|Car|Nissan|Sylphy|CAK-4406|Standard|7000.0|52000|true|5|Petrol|cars/nissan-sylphy.jpg",
            "V009|Car|Toyota|RAV4|CAL-9035|Luxury|13000.0|19000|true|5|Hybrid|cars/toyota-rav4.jpg",
            "V010|Car|Toyota|Vitz|CAM-1188|Economy|4000.0|64000|true|5|Petrol|cars/toyota-vitz.jpg",
            "V011|Van|Toyota|HiAce|PH-4521|Standard|12000.0|88000|true|1200|vans/toyota-hiace.jpg",
            "V012|Van|Nissan|NV350|PH-7788|Standard|11500.0|95000|true|1200|vans/nissan-nv350.jpg",
            "V013|Van|Ford|Transit|PH-3302|Standard|13000.0|70000|true|1400|vans/ford-transit.jpg",
            "V014|Van|Hyundai|H1|PH-6615|Standard|11000.0|82000|true|900|vans/hyundai-h1.jpg",
            "V015|Van|Mitsubishi|Delica|PH-2290|Economy|9500.0|110000|true|800|vans/mitsubishi-delica.jpg",
            "V016|Van|Nissan|Serena|PH-5147|Economy|9000.0|76000|true|600|vans/nissan-serena.jpg",
            "V017|Van|Renault|Trafic|PH-8834|Standard|12500.0|61000|true|1200|vans/renault-trafic.jpg",
            "V018|Van|Toyota|Granvia|PH-1973|Luxury|15000.0|54000|true|700|vans/toyota-granvia.jpg",
            "V019|Van|Toyota|Noah|PH-4460|Economy|8500.0|89000|true|500|vans/toyota-noah.jpg",
            "V020|Van|Volkswagen|Transporter|PH-7051|Luxury|14000.0|47000|true|1300|vans/volkswagen-transporter.jpg",
            "V021|Motorcycle|Bajaj|Pulsar 150|BBK-9087|Economy|2500.0|15000|true|150|motorcycles/bajaj-pulsar-150.jpg",
            "V022|Motorcycle|Honda|CB125F|BBL-2214|Economy|2200.0|12000|true|125|motorcycles/honda-cb125f.jpg",
            "V023|Motorcycle|Honda|Dio|BBM-6630|Economy|1800.0|9000|true|110|motorcycles/honda-dio.jpg",
            "V024|Motorcycle|KTM|Duke 200|BBN-4471|Luxury|4500.0|7000|true|200|motorcycles/ktm-duke-200.jpg",
            "V025|Motorcycle|Royal Enfield|Classic 350|BBP-8125|Luxury|5000.0|11000|true|350|motorcycles/royal-enfield-classic-350.jpg",
            "V026|Motorcycle|Suzuki|Access 125|BBQ-3307|Economy|2000.0|14000|true|125|motorcycles/suzuki-access-125.jpg",
            "V027|Motorcycle|TVS|Apache RTR 160|BBR-5592|Standard|2800.0|16000|true|160|motorcycles/tvs-apache-rtr-160.jpg",
            "V028|Motorcycle|Yamaha|FZ-S|BBS-7743|Standard|3000.0|8000|true|149|motorcycles/yamaha-fz-s.jpg",
            "V029|Motorcycle|Yamaha|R15|BBT-1069|Luxury|4200.0|6000|true|155|motorcycles/yamaha-r15.jpg",
            "V030|Motorcycle|Yamaha|Ray ZR|BBU-2958|Economy|1900.0|10000|true|125|motorcycles/yamaha-ray-zr.jpg"
        };
        return lines;
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
