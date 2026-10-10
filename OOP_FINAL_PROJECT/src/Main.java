
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        VehicleManager manager = new VehicleManager();

        int choice;

        do {
            System.out.println("\n===== VEHICLE RENTAL SERVICE =====");
            System.out.println("1. Add Vehicle");
            System.out.println("2. Display All Vehicles");
            System.out.println("3. Search Vehicle by ID");
            System.out.println("4. Search Vehicle by Brand");
            System.out.println("5. Search Vehicle by Type");
            System.out.println("6. Update Vehicle");
            System.out.println("7. Delete Vehicle");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            choice = readInt(input);

            switch (choice) {
                case 1:
                    addVehicle(input, manager);
                    break;

                case 2:
                    manager.displayAllVehicles();
                    break;

                case 3:
                    System.out.print("Enter Vehicle ID: ");
                    String searchId = input.nextLine();

                    Vehicle found = manager.searchById(searchId);

                    if (found != null) {
                        found.displayDetails();
                    } else {
                        System.out.println("Vehicle not found.");
                    }
                    break;

                case 4:
                    System.out.print("Enter Brand: ");
                    String brand = input.nextLine();

                    ArrayList<Vehicle> brandResults =
                            manager.searchByBrand(brand);

                    displayResults(brandResults);
                    break;

                case 5:
                    System.out.print(
                            "Enter Type (Car, Van, Motorcycle): ");
                    String type = input.nextLine();

                    ArrayList<Vehicle> typeResults =
                            manager.searchByType(type);

                    displayResults(typeResults);
                    break;

                case 6:
                    updateVehicle(input, manager);
                    break;

                case 7:
                    System.out.print("Enter Vehicle ID to delete: ");
                    String deleteId = input.nextLine();

                    manager.deleteVehicle(deleteId);
                    break;

                case 0:
                    System.out.println("Program closed.");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 0);

        input.close();
    }

    // ADD VEHICLE
    public static void addVehicle(
            Scanner input, VehicleManager manager) {

        System.out.print("Enter Vehicle ID: ");
        String id = input.nextLine();

        System.out.print("Enter Brand: ");
        String brand = input.nextLine();

        System.out.print("Enter Model: ");
        String model = input.nextLine();

        System.out.print("Enter Type (Car, Van, Motorcycle): ");
        String type = input.nextLine().trim();

        System.out.print("Enter Plate Number: ");
        String plate = input.nextLine();

        System.out.print("Enter Base Rental Rate: ");
        double rate = readDouble(input);

        System.out.print("Enter Availability: ");
        String availability = input.nextLine();

        System.out.print("Enter Mileage: ");
        double mileage = readDouble(input);

        Vehicle vehicle;

        switch (type.toLowerCase()) {
            case "car":
                System.out.print("Enter Number of Seats: ");
                int seats = readInt(input);
                vehicle = new Car(seats);
                break;

            case "van":
                System.out.print("Enter Passenger Capacity: ");
                int capacity = readInt(input);
                vehicle = new Van(capacity);
                break;

            case "motorcycle":
                System.out.print("Enter Engine Capacity (cc): ");
                double engineCapacity = readDouble(input);
                vehicle = new MotorCycle(engineCapacity);
                break;

            default:
                System.out.println("Invalid vehicle type.");
                return;
        }

        vehicle.setVehicleId(id);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setType(type);
        vehicle.setPlateNumber(plate);
        vehicle.setRentalRate(rate);
        vehicle.setAvailability(availability);
        vehicle.setMileage(mileage);

        manager.addVehicle(vehicle);
    }

    // UPDATE VEHICLE
    public static void updateVehicle(
            Scanner input, VehicleManager manager) {

        System.out.print("Enter Vehicle ID to update: ");
        String id = input.nextLine();

        Vehicle existing = manager.searchById(id);

        if (existing == null) {
            System.out.println("Vehicle not found.");
            return;
        }

        System.out.println("Current vehicle details:");
        existing.displayDetails();

        System.out.println("\nEnter the new details:");

        System.out.print("Enter Brand: ");
        String brand = input.nextLine();

        System.out.print("Enter Model: ");
        String model = input.nextLine();

        System.out.print("Enter Plate Number: ");
        String plate = input.nextLine();

        System.out.print("Enter Base Rental Rate: ");
        double rate = readDouble(input);

        System.out.print("Enter Availability: ");
        String availability = input.nextLine();

        System.out.print("Enter Mileage: ");
        double mileage = readDouble(input);

        Vehicle updated;

        if (existing instanceof Car) {
            System.out.print("Enter Number of Seats: ");
            int seats = readInt(input);
            updated = new Car(seats);

        } else if (existing instanceof Van) {
            System.out.print("Enter Passenger Capacity: ");
            int capacity = readInt(input);
            updated = new Van(capacity);

        } else if (existing instanceof MotorCycle) {
            System.out.print("Enter Engine Capacity (cc): ");
            double engineCapacity = readDouble(input);
            updated = new MotorCycle(engineCapacity);

        } else {
            System.out.println("Unsupported vehicle type.");
            return;
        }

        updated.setVehicleId(existing.getVehicleId());
        updated.setBrand(brand);
        updated.setModel(model);
        updated.setType(existing.getType());
        updated.setPlateNumber(plate);
        updated.setRentalRate(rate);
        updated.setAvailability(availability);
        updated.setMileage(mileage);

        manager.updateVehicle(id, updated);
    }

    // DISPLAY SEARCH RESULTS
    public static void displayResults(
            ArrayList<Vehicle> vehicles) {

        if (vehicles.isEmpty()) {
            System.out.println("No vehicles found.");
            return;
        }

        for (Vehicle vehicle : vehicles) {
            vehicle.displayDetails();
            System.out.println("----------------------------");
        }
    }

    // READ INTEGER INPUT
    public static int readInt(Scanner input) {

        while (true) {
            try {
                int value = Integer.parseInt(input.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid whole number: ");
            }
        }
    }

    // READ DECIMAL INPUT
    public static double readDouble(Scanner input) {

        while (true) {
            try {
                double value = Double.parseDouble(input.nextLine().trim());

                if (!Double.isFinite(value)) {
                    System.out.print("Enter a valid number: ");
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}
