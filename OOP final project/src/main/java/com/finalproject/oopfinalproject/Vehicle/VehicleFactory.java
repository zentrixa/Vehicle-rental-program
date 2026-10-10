package com.finalproject.oopfinalproject.Vehicle;

public class VehicleFactory {
    public static Vehicle createVehicle(String type, String vehicleId, String brand, String model,
                                        String plateNumber, String category, double dailyRate,
                                        int mileage, boolean available,
                                        int seats, String fuelType, int cargoCapacity, int engineCc) {
        if (type == null) {
            throw new IllegalArgumentException("Choose a vehicle type: Car, Van or Motorcycle.");
        }
        if (type.equalsIgnoreCase("Car")) {
            return new Car(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available,
                    seats, fuelType);
        } else if (type.equalsIgnoreCase("Van")) {
            return new Van(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available,
                    cargoCapacity);
        } else if (type.equalsIgnoreCase("Motorcycle")) {
            return new Motorcycle(vehicleId, brand, model, plateNumber, category, dailyRate, mileage, available,
                    engineCc);
        }
        throw new IllegalArgumentException("Choose a vehicle type: Car, Van or Motorcycle.");
    }

    public static Vehicle fromFileString(String line) {
        String[] parts = line.split("\\|");
        try {
            String id = parts[0];
            String type = parts[1];
            String brand = parts[2];
            String model = parts[3];
            String plate = parts[4];
            String category = parts[5];
            double dailyRate = Double.parseDouble(parts[6]);
            int mileage = Integer.parseInt(parts[7]);
            boolean available = Boolean.parseBoolean(parts[8]);

            int seats = 0;
            String fuelType = "";
            int cargoCapacity = 0;
            int engineCc = 0;

            if (type.equalsIgnoreCase("Car")) {
                seats = Integer.parseInt(parts[9]);
                fuelType = parts[10];
            } else if (type.equalsIgnoreCase("Van")) {
                cargoCapacity = Integer.parseInt(parts[9]);
            } else if (type.equalsIgnoreCase("Motorcycle")) {
                engineCc = Integer.parseInt(parts[9]);
            }

            return createVehicle(type, id, brand, model, plate, category, dailyRate, mileage, available,
                    seats, fuelType, cargoCapacity, engineCc);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Skipping a damaged line in vehicles.txt: " + line);
            return null;
        }
    }
}
