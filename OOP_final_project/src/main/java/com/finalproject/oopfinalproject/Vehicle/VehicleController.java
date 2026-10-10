package com.finalproject.oopfinalproject.Vehicle;

import com.finalproject.oopfinalproject.User.Account;
import com.finalproject.oopfinalproject.User.AccountRepository;
import com.finalproject.oopfinalproject.User.Customer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VehicleController {
    private VehicleManager vehicleManager;
    private AccountRepository accountRepository;

    public VehicleController(VehicleManager vehicleManager, AccountRepository accountRepository) {
        this.vehicleManager = vehicleManager;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/api/vehicles")
    public ResponseEntity<Vehicle[]> listVehicles(@RequestParam(defaultValue = "") String brand,
                                                  @RequestParam(defaultValue = "") String type,
                                                  @RequestParam(defaultValue = "") String category,
                                                  HttpServletRequest request) {
        if (getLoggedInAccount(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Vehicle[] vehicles = vehicleManager.searchVehicles(brand, type, category);
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(vehicles);
    }

    @GetMapping("/api/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable String id, HttpServletRequest request) {
        if (getLoggedInAccount(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(vehicle);
    }

    @GetMapping("/api/vehicles/{id}/cost")
    public ResponseEntity<VehicleResponse> getRentalCost(@PathVariable String id,
                                                         @RequestParam(defaultValue = "1") int days,
                                                         HttpServletRequest request) {
        Account account = getLoggedInAccount(request);
        if (account == null) {
            return reply(HttpStatus.UNAUTHORIZED, false, "Log in to see prices.");
        }
        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            return reply(HttpStatus.NOT_FOUND, false, "No vehicle has the ID " + id + ". It may have been deleted.");
        }
        if (!vehicle.isValidRentalDays(days)) {
            return reply(HttpStatus.BAD_REQUEST, false, "Rental days must be between 1 and 365.");
        }

        double cost;
        String note = "";
        if (isPremiumCustomer(account)) {
            cost = vehicle.calculateRentalCost(days, 5);
            note = " (includes your 5% Premium discount)";
        } else {
            cost = vehicle.calculateRentalCost(days);
        }

        String dayWord = " days";
        if (days == 1) {
            dayWord = " day";
        }
        return reply(HttpStatus.OK, true, formatMoney(cost) + " for " + days + dayWord + note + ".");
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
                                                      HttpServletRequest request) {
        ResponseEntity<VehicleResponse> denied = checkAdmin(request);
        if (denied != null) {
            return denied;
        }

        Vehicle vehicle = vehicleManager.createVehicle(type, "", brand, model, plateNumber, category,
                dailyRate, mileage, available, seats, fuelType, cargoCapacity, engineCc);
        if (vehicle == null) {
            return reply(HttpStatus.BAD_REQUEST, false, "Choose a vehicle type: Car, Van or Motorcycle.");
        }
        String problem = vehicle.checkDetails();
        if (!problem.equals("")) {
            return reply(HttpStatus.BAD_REQUEST, false, problem);
        }
        if (vehicleManager.isPlateNumberTaken(vehicle.getPlateNumber(), "")) {
            return reply(HttpStatus.CONFLICT, false,
                    "Another vehicle already has the plate number " + vehicle.getPlateNumber() + ".");
        }
        if (!vehicleManager.addVehicle(vehicle)) {
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "We couldn't save the vehicle. Try again.");
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
                                                         HttpServletRequest request) {
        ResponseEntity<VehicleResponse> denied = checkAdmin(request);
        if (denied != null) {
            return denied;
        }

        Vehicle existing = vehicleManager.findVehicleById(id);
        if (existing == null) {
            return reply(HttpStatus.NOT_FOUND, false, "No vehicle has the ID " + id + ". It may have been deleted.");
        }

        Vehicle updated = vehicleManager.createVehicle(existing.getVehicleType(), existing.getVehicleId(),
                brand, model, plateNumber, category, dailyRate, mileage, available,
                seats, fuelType, cargoCapacity, engineCc);
        String problem = updated.checkDetails();
        if (!problem.equals("")) {
            return reply(HttpStatus.BAD_REQUEST, false, problem);
        }
        if (vehicleManager.isPlateNumberTaken(updated.getPlateNumber(), updated.getVehicleId())) {
            return reply(HttpStatus.CONFLICT, false,
                    "Another vehicle already has the plate number " + updated.getPlateNumber() + ".");
        }
        if (!vehicleManager.updateVehicle(updated)) {
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "We couldn't save the changes. Try again.");
        }
        return reply(HttpStatus.OK, true, "Saved the changes to " + updated.getVehicleId() + ".");
    }

    @DeleteMapping("/api/vehicles/{id}")
    public ResponseEntity<VehicleResponse> deleteVehicle(@PathVariable String id, HttpServletRequest request) {
        ResponseEntity<VehicleResponse> denied = checkAdmin(request);
        if (denied != null) {
            return denied;
        }

        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            return reply(HttpStatus.NOT_FOUND, false, "No vehicle has the ID " + id + ". It may have been deleted.");
        }
        if (!vehicleManager.deleteVehicle(vehicle.getVehicleId())) {
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "We couldn't delete the vehicle. Try again.");
        }
        return reply(HttpStatus.OK, true, "Deleted " + vehicle.getBrand() + " " + vehicle.getModel()
                + " (" + vehicle.getVehicleId() + ").");
    }

    private Account getLoggedInAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object username = session.getAttribute("username");
        if (username == null) {
            return null;
        }
        return accountRepository.findByUsername(username.toString()).orElse(null);
    }

    private ResponseEntity<VehicleResponse> checkAdmin(HttpServletRequest request) {
        Account account = getLoggedInAccount(request);
        if (account == null) {
            return reply(HttpStatus.UNAUTHORIZED, false, "Log in as an admin to do this.");
        }
        if (!account.getRole().equals("ADMIN")) {
            return reply(HttpStatus.FORBIDDEN, false, "Only admins can add, edit or delete vehicles.");
        }
        return null;
    }

    private boolean isPremiumCustomer(Account account) {
        if (!account.getRole().equals("CUSTOMER")) {
            return false;
        }
        List<Customer> customers = accountRepository.findAllCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.getUsername().equalsIgnoreCase(account.getUsername())) {
                return customer.isPremium();
            }
        }
        return false;
    }

    private String formatMoney(double amount) {
        long cents = Math.round(amount * 100);
        long rupees = cents / 100;
        long remainder = cents % 100;
        if (remainder < 10) {
            return "Rs. " + rupees + ".0" + remainder;
        }
        return "Rs. " + rupees + "." + remainder;
    }

    private ResponseEntity<VehicleResponse> reply(HttpStatus status, boolean ok, String message) {
        return ResponseEntity.status(status).body(new VehicleResponse(ok, message));
    }
}
