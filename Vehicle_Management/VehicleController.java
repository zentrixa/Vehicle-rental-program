package com.finalproject.oopfinalproject.Vehicle;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VehicleController {
    private VehicleManager vehicleManager;

    public VehicleController(VehicleManager vehicleManager) {
        this.vehicleManager = vehicleManager;
    }

    @GetMapping("/api/vehicles")
    public ResponseEntity<Vehicle[]> listVehicles(@RequestParam(defaultValue = "") String brand,
                                                  @RequestParam(defaultValue = "") String type,
                                                  @RequestParam(defaultValue = "") String category) {
        return ResponseEntity.ok(vehicleManager.searchVehicles(brand, type, category));
    }

    @GetMapping("/api/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable String id) {
        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/api/vehicles/{id}/cost")
    public ResponseEntity<VehicleResponse> getRentalCost(@PathVariable String id,
                                                         @RequestParam(defaultValue = "1") int days,
                                                         @RequestParam(defaultValue = "false") boolean premium) {
        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            return reply(HttpStatus.NOT_FOUND, false, "Vehicle not found.");
        }
        if (!vehicle.isValidRentalDays(days)) {
            return reply(HttpStatus.BAD_REQUEST, false, "Rental days must be between 1 and 365.");
        }

        double cost;
        String note = "";
        if (premium) {
            cost = vehicle.calculateRentalCost(days, 5);
            note = " (5% Premium discount included)";
        } else {
            cost = vehicle.calculateRentalCost(days);
        }
        return reply(HttpStatus.OK, true, formatMoney(cost) + " for " + days + " day(s)" + note);
    }

    @PostMapping("/api/vehicles")
    public ResponseEntity<VehicleResponse> addVehicle(@RequestParam(defaultValue = "") String type,
                                                      @RequestParam(defaultValue = "") String brand,
                                                      @RequestParam(defaultValue = "") String model,
                                                      @RequestParam(defaultValue = "") String plateNumber,
                                                      @RequestParam(defaultValue = "") String category,
                                                      @RequestParam(defaultValue = "0") double dailyRate,
                                                      @RequestParam(defaultValue = "-1") int mileage,
                                                      @RequestParam(defaultValue = "true") boolean available,
                                                      @RequestParam(defaultValue = "0") int seats,
                                                      @RequestParam(defaultValue = "") String fuelType,
                                                      @RequestParam(defaultValue = "0") int cargoCapacity,
                                                      @RequestParam(defaultValue = "0") int engineCc,
                                                      @RequestParam(defaultValue = "") String imageName) {
        Vehicle vehicle = vehicleManager.createVehicle(type, "", brand, model, plateNumber, category,
                dailyRate, mileage, available, seats, fuelType, cargoCapacity, engineCc);
        if (vehicle == null) {
            return reply(HttpStatus.BAD_REQUEST, false, "Choose a vehicle type: Car, Van or Motorcycle.");
        }
        vehicle.setImageName(imageName);

        String problem = vehicle.checkDetails();
        if (!problem.equals("")) {
            return reply(HttpStatus.BAD_REQUEST, false, problem);
        }
        if (vehicleManager.isPlateNumberTaken(vehicle.getPlateNumber(), "")) {
            return reply(HttpStatus.CONFLICT, false,
                    "Another vehicle already has the plate number " + vehicle.getPlateNumber() + ".");
        }
        if (!vehicleManager.addVehicle(vehicle)) {
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "Could not save the vehicle.");
        }
        return reply(HttpStatus.CREATED, true, "Added " + vehicle.getBrand() + " " + vehicle.getModel()
                + " as " + vehicle.getVehicleId() + ".");
    }

    @PutMapping("/api/vehicles/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable String id,
                                                         @RequestParam(defaultValue = "") String brand,
                                                         @RequestParam(defaultValue = "") String model,
                                                         @RequestParam(defaultValue = "") String plateNumber,
                                                         @RequestParam(defaultValue = "") String category,
                                                         @RequestParam(defaultValue = "0") double dailyRate,
                                                         @RequestParam(defaultValue = "-1") int mileage,
                                                         @RequestParam(defaultValue = "true") boolean available,
                                                         @RequestParam(defaultValue = "0") int seats,
                                                         @RequestParam(defaultValue = "") String fuelType,
                                                         @RequestParam(defaultValue = "0") int cargoCapacity,
                                                         @RequestParam(defaultValue = "0") int engineCc,
                                                         @RequestParam(defaultValue = "") String imageName) {
        Vehicle existing = vehicleManager.findVehicleById(id);
        if (existing == null) {
            return reply(HttpStatus.NOT_FOUND, false, "Vehicle not found.");
        }

        Vehicle updated = vehicleManager.createVehicle(existing.getVehicleType(), existing.getVehicleId(),
                brand, model, plateNumber, category, dailyRate, mileage, available,
                seats, fuelType, cargoCapacity, engineCc);
        updated.setImageName(imageName);

        String problem = updated.checkDetails();
        if (!problem.equals("")) {
            return reply(HttpStatus.BAD_REQUEST, false, problem);
        }
        if (vehicleManager.isPlateNumberTaken(updated.getPlateNumber(), updated.getVehicleId())) {
            return reply(HttpStatus.CONFLICT, false,
                    "Another vehicle already has the plate number " + updated.getPlateNumber() + ".");
        }
        if (!vehicleManager.updateVehicle(updated)) {
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "Could not save the changes.");
        }
        return reply(HttpStatus.OK, true, "Saved the changes to " + updated.getVehicleId() + ".");
    }

    @DeleteMapping("/api/vehicles/{id}")
    public ResponseEntity<VehicleResponse> deleteVehicle(@PathVariable String id) {
        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            return reply(HttpStatus.NOT_FOUND, false, "Vehicle not found.");
        }
        if (!vehicleManager.deleteVehicle(vehicle.getVehicleId())) {
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "Could not delete the vehicle.");
        }
        return reply(HttpStatus.OK, true, "Deleted " + vehicle.getBrand() + " " + vehicle.getModel()
                + " (" + vehicle.getVehicleId() + ").");
    }

    private String formatMoney(double amount) {
        return "Rs. " + (Math.round(amount * 100) / 100.0);
    }

    private ResponseEntity<VehicleResponse> reply(HttpStatus status, boolean ok, String message) {
        return ResponseEntity.status(status).body(new VehicleResponse(ok, message));
    }
}
