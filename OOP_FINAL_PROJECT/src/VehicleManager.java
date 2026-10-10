
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class VehicleManager {

    private final String fileName = "vehicles.txt";

    // CREATE
    public void addVehicle(Vehicle vehicle) {

        if (vehicle == null || vehicle.getVehicleId() == null
                || vehicle.getVehicleId().trim().isEmpty()) {
            System.out.println("Invalid vehicle or vehicle ID.");
            return;
        }

        ArrayList<Vehicle> vehicles = getAllVehicles();

        for (Vehicle existing : vehicles) {
            if (existing.getVehicleId().equalsIgnoreCase(
                    vehicle.getVehicleId().trim())) {
                System.out.println("Vehicle ID already exists.");
                return;
            }
        }

        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(vehicleToLine(vehicle) + System.lineSeparator());
            System.out.println("Vehicle added successfully.");
        } catch (IOException e) {
            System.out.println("Error saving vehicle.");
            e.printStackTrace();
        }
    }

    // READ: GET ALL VEHICLES
    public ArrayList<Vehicle> getAllVehicles() {

        ArrayList<Vehicle> vehicles = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) {
            return vehicles;
        }

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Vehicle vehicle = lineToVehicle(line);

                    if (vehicle != null) {
                        vehicles.add(vehicle);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Skipping invalid record: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading vehicles.");
            e.printStackTrace();
        }

        return vehicles;
    }

    // READ: SEARCH BY ID
    public Vehicle searchById(String vehicleId) {

        if (vehicleId == null) {
            return null;
        }

        for (Vehicle vehicle : getAllVehicles()) {
            if (vehicle.getVehicleId().equalsIgnoreCase(vehicleId.trim())) {
                return vehicle;
            }
        }

        return null;
    }

    // READ: SEARCH BY BRAND
    public ArrayList<Vehicle> searchByBrand(String brand) {

        ArrayList<Vehicle> results = new ArrayList<>();

        if (brand == null) {
            return results;
        }

        for (Vehicle vehicle : getAllVehicles()) {
            if (vehicle.getBrand() != null
                    && vehicle.getBrand().toLowerCase()
                    .contains(brand.trim().toLowerCase())) {
                results.add(vehicle);
            }
        }

        return results;
    }

    // READ: SEARCH BY TYPE
    public ArrayList<Vehicle> searchByType(String type) {

        ArrayList<Vehicle> results = new ArrayList<>();

        if (type == null) {
            return results;
        }

        for (Vehicle vehicle : getAllVehicles()) {
            if (vehicle.getType() != null
                    && vehicle.getType().equalsIgnoreCase(type.trim())) {
                results.add(vehicle);
            }
        }

        return results;
    }

    // UPDATE
    public void updateVehicle(String vehicleId, Vehicle updatedVehicle) {

        if (vehicleId == null || updatedVehicle == null) {
            System.out.println("Invalid vehicle details.");
            return;
        }

        ArrayList<Vehicle> vehicles = getAllVehicles();
        boolean found = false;

        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getVehicleId()
                    .equalsIgnoreCase(vehicleId.trim())) {

                updatedVehicle.setVehicleId(
                        vehicles.get(i).getVehicleId());

                vehicles.set(i, updatedVehicle);
                found = true;
                break;
            }
        }

        if (found) {
            if (saveAllVehicles(vehicles)) {
                System.out.println("Vehicle updated successfully.");
            }
        } else {
            System.out.println("Vehicle not found.");
        }
    }

    // DELETE
    public void deleteVehicle(String vehicleId) {

        if (vehicleId == null) {
            System.out.println("Invalid vehicle ID.");
            return;
        }

        ArrayList<Vehicle> vehicles = getAllVehicles();

        boolean removed = vehicles.removeIf(
                vehicle -> vehicle.getVehicleId()
                        .equalsIgnoreCase(vehicleId.trim())
        );

        if (removed) {
            if (saveAllVehicles(vehicles)) {
                System.out.println("Vehicle deleted successfully.");
            }
        } else {
            System.out.println("Vehicle not found.");
        }
    }

    // DISPLAY ALL VEHICLES
    public void displayAllVehicles() {

        ArrayList<Vehicle> vehicles = getAllVehicles();

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles found.");
            return;
        }

        for (Vehicle vehicle : vehicles) {
            vehicle.displayDetails();
            System.out.println("----------------------------");
        }
    }

    // CONVERT VEHICLE OBJECT TO TEXT
    private String vehicleToLine(Vehicle vehicle) {

        String specialValue;

        if (vehicle instanceof Car) {
            specialValue = String.valueOf(
                    ((Car) vehicle).getNumberOfSeats());

        } else if (vehicle instanceof Van) {
            specialValue = String.valueOf(
                    ((Van) vehicle).getPassengerCapacity());

        } else if (vehicle instanceof MotorCycle) {
            specialValue = String.valueOf(
                    ((MotorCycle) vehicle).getEngineCapacity());

        } else {
            specialValue = "0";
        }

        return clean(vehicle.getVehicleId()) + "|"
                + clean(vehicle.getBrand()) + "|"
                + clean(vehicle.getModel()) + "|"
                + clean(vehicle.getType()) + "|"
                + clean(vehicle.getPlateNumber()) + "|"
                + vehicle.getRentalRate() + "|"
                + clean(vehicle.getAvailability()) + "|"
                + vehicle.getMileage() + "|"
                + specialValue;
    }

    // CONVERT TEXT TO VEHICLE OBJECT
    private Vehicle lineToVehicle(String line) {

        String[] data = line.split("\\|", -1);

        // Accept old 8-field records and new 9-field records.
        if (data.length != 8 && data.length != 9) {
            throw new IllegalArgumentException("Invalid vehicle record.");
        }

        String type = data[3].trim();
        Vehicle vehicle;

        switch (type.toLowerCase()) {
            case "car":
                vehicle = new Car(5);
                break;

            case "van":
                vehicle = new Van(10);
                break;

            case "motorcycle":
                vehicle = new MotorCycle(150);
                break;

            default:
                throw new IllegalArgumentException("Unknown vehicle type.");
        }

        vehicle.setVehicleId(data[0]);
        vehicle.setBrand(data[1]);
        vehicle.setModel(data[2]);
        vehicle.setPlateNumber(data[4]);
        vehicle.setRentalRate(Double.parseDouble(data[5]));
        vehicle.setAvailability(data[6]);
        vehicle.setMileage(Double.parseDouble(data[7]));

        // Restore the special property if the record has 9 fields.
        if (data.length == 9) {

            if (vehicle instanceof Car) {
                ((Car) vehicle).setNumberOfSeats(
                        Integer.parseInt(data[8]));

            } else if (vehicle instanceof Van) {
                ((Van) vehicle).setPassengerCapacity(
                        Integer.parseInt(data[8]));

            } else if (vehicle instanceof MotorCycle) {
                ((MotorCycle) vehicle).setEngineCapacity(
                        Double.parseDouble(data[8]));
            }
        }

        return vehicle;
    }

    // SAVE ALL RECORDS AFTER UPDATE OR DELETE
    private boolean saveAllVehicles(ArrayList<Vehicle> vehicles) {

        try (FileWriter writer = new FileWriter(fileName, false)) {
            for (Vehicle vehicle : vehicles) {
                writer.write(vehicleToLine(vehicle)
                        + System.lineSeparator());
            }

            return true;

        } catch (IOException e) {
            System.out.println("Error saving vehicle records.");
            e.printStackTrace();
            return false;
        }
    }

    // REMOVE CHARACTERS THAT BREAK THE FILE FORMAT
    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("|", "/")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
