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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@RestController
public class VehicleController {
    private static final String SESSION_USERNAME = "username";

    private static final double PREMIUM_DISCOUNT_PERCENT = 5.0;

    private final VehicleRepository vehicleRepository;
    private final AccountRepository accountRepository;

    public VehicleController(VehicleRepository vehicleRepository, AccountRepository accountRepository) {
        this.vehicleRepository = vehicleRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/api/vehicles")
    public ResponseEntity<ArrayList<Vehicle>> listVehicles(@RequestParam(defaultValue = "") String brand,
                                                           @RequestParam(defaultValue = "") String type,
                                                           @RequestParam(defaultValue = "") String category,
                                                           HttpServletRequest request) {
        if (getLoggedInAccount(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            ArrayList<Vehicle> vehicles = vehicleRepository.searchVehicles(brand, type, category);
            return ResponseEntity.ok().header("Cache-Control", "no-store").body(vehicles);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable String id, HttpServletRequest request) {
        if (getLoggedInAccount(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Vehicle vehicle = vehicleRepository.findById(id);
            return ResponseEntity.ok().header("Cache-Control", "no-store").body(vehicle);
        } catch (VehicleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/vehicles/{id}/cost")
    public ResponseEntity<VehicleResponse> getRentalCost(@PathVariable String id,
                                                         @RequestParam(defaultValue = "1") String days,
                                                         HttpServletRequest request) {
        Account account = getLoggedInAccount(request);
        if (account == null) {
            return reply(HttpStatus.UNAUTHORIZED, false, "Log in to see prices.");
        }
        try {
            int numberOfDays = readWholeNumber(days, "the number of days");
            Vehicle vehicle = vehicleRepository.findById(id);

            double cost;
            String note = "";

            if (account instanceof Customer && ((Customer) account).isPremium()) {
                cost = vehicle.calculateRentalCost(numberOfDays, PREMIUM_DISCOUNT_PERCENT);
                note = " (includes your 5% Premium discount)";
            } else {
                cost = vehicle.calculateRentalCost(numberOfDays);
            }

            String dayWord = numberOfDays == 1 ? " day" : " days";
            String message = String.format("Rs. %,.2f", cost) + " for " + numberOfDays + dayWord + note + ".";
            return reply(HttpStatus.OK, true, message);
        } catch (IllegalArgumentException e) {
            return reply(HttpStatus.BAD_REQUEST, false, e.getMessage());
        } catch (VehicleNotFoundException e) {
            return reply(HttpStatus.NOT_FOUND, false, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "We couldn't work out the price. Try again.");
        }
    }

    @PostMapping("/api/vehicles")
    public ResponseEntity<VehicleResponse> addVehicle(@RequestParam(defaultValue = "") String type,
                                                      @RequestParam(defaultValue = "") String brand,
                                                      @RequestParam(defaultValue = "") String model,
                                                      @RequestParam(defaultValue = "") String plateNumber,
                                                      @RequestParam(defaultValue = "") String category,
                                                      @RequestParam(defaultValue = "") String dailyRate,
                                                      @RequestParam(defaultValue = "") String mileage,
                                                      @RequestParam(defaultValue = "true") String available,
                                                      @RequestParam(defaultValue = "") String seats,
                                                      @RequestParam(defaultValue = "") String fuelType,
                                                      @RequestParam(defaultValue = "") String cargoCapacity,
                                                      @RequestParam(defaultValue = "") String engineCc,
                                                      HttpServletRequest request) {
        ResponseEntity<VehicleResponse> denied = checkAdmin(request);
        if (denied != null) {
            return denied;
        }

        try {
            Vehicle vehicle = buildVehicle(type, "", brand, model, plateNumber, category, dailyRate,
                    mileage, available, seats, fuelType, cargoCapacity, engineCc);
            vehicleRepository.addVehicle(vehicle);
            return reply(HttpStatus.CREATED, true, "Added " + vehicle.getBrand() + " " + vehicle.getModel()
                    + " as " + vehicle.getVehicleId() + ".");
        } catch (IllegalArgumentException e) {
            return reply(HttpStatus.BAD_REQUEST, false, e.getMessage());
        } catch (DuplicatePlateNumberException e) {
            return reply(HttpStatus.CONFLICT, false, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "We couldn't save the vehicle. Try again.");
        }
    }

    @PutMapping("/api/vehicles/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable String id,
                                                         @RequestParam(defaultValue = "") String brand,
                                                         @RequestParam(defaultValue = "") String model,
                                                         @RequestParam(defaultValue = "") String plateNumber,
                                                         @RequestParam(defaultValue = "") String category,
                                                         @RequestParam(defaultValue = "") String dailyRate,
                                                         @RequestParam(defaultValue = "") String mileage,
                                                         @RequestParam(defaultValue = "true") String available,
                                                         @RequestParam(defaultValue = "") String seats,
                                                         @RequestParam(defaultValue = "") String fuelType,
                                                         @RequestParam(defaultValue = "") String cargoCapacity,
                                                         @RequestParam(defaultValue = "") String engineCc,
                                                         HttpServletRequest request) {
        ResponseEntity<VehicleResponse> denied = checkAdmin(request);
        if (denied != null) {
            return denied;
        }

        try {
            Vehicle existing = vehicleRepository.findById(id);
            Vehicle updated = buildVehicle(existing.getVehicleType(), existing.getVehicleId(), brand, model,
                    plateNumber, category, dailyRate, mileage, available, seats, fuelType, cargoCapacity, engineCc);
            vehicleRepository.updateVehicle(updated);
            return reply(HttpStatus.OK, true, "Saved the changes to " + updated.getVehicleId() + ".");
        } catch (IllegalArgumentException e) {
            return reply(HttpStatus.BAD_REQUEST, false, e.getMessage());
        } catch (DuplicatePlateNumberException e) {
            return reply(HttpStatus.CONFLICT, false, e.getMessage());
        } catch (VehicleNotFoundException e) {
            return reply(HttpStatus.NOT_FOUND, false, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "We couldn't save the changes. Try again.");
        }
    }

    @DeleteMapping("/api/vehicles/{id}")
    public ResponseEntity<VehicleResponse> deleteVehicle(@PathVariable String id, HttpServletRequest request) {
        ResponseEntity<VehicleResponse> denied = checkAdmin(request);
        if (denied != null) {
            return denied;
        }

        try {
            Vehicle removed = vehicleRepository.deleteVehicle(id);
            return reply(HttpStatus.OK, true, "Deleted " + removed.getBrand() + " " + removed.getModel()
                    + " (" + removed.getVehicleId() + ").");
        } catch (VehicleNotFoundException e) {
            return reply(HttpStatus.NOT_FOUND, false, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, false, "We couldn't delete the vehicle. Try again.");
        }
    }

    private Vehicle buildVehicle(String type, String vehicleId, String brand, String model, String plateNumber,
                                 String category, String dailyRate, String mileage, String available,
                                 String seats, String fuelType, String cargoCapacity, String engineCc) {
        double rate = readDecimalNumber(dailyRate, "the daily rate");
        int km = readWholeNumber(mileage, "the mileage");
        boolean isAvailable = available.trim().equalsIgnoreCase("true");

        int seatCount = 0;
        int cargo = 0;
        int cc = 0;
        if (type.equalsIgnoreCase("Car")) {
            seatCount = readWholeNumber(seats, "the number of seats");
        } else if (type.equalsIgnoreCase("Van")) {
            cargo = readWholeNumber(cargoCapacity, "the cargo capacity");
        } else if (type.equalsIgnoreCase("Motorcycle")) {
            cc = readWholeNumber(engineCc, "the engine size");
        }

        return VehicleFactory.createVehicle(type, vehicleId, brand, model, plateNumber, category,
                rate, km, isAvailable, seatCount, fuelType, cargo, cc);
    }

    private int readWholeNumber(String text, String label) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter " + label + " as a whole number.");
        }
    }

    private double readDecimalNumber(String text, String label) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter " + label + " as a number.");
        }
    }

    private Account getLoggedInAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object username = session.getAttribute(SESSION_USERNAME);
        if (username == null) {
            return null;
        }

        Optional<Account> account = accountRepository.findByUsername(username.toString());
        if (account.isPresent()) {
            return account.get();
        }
        return null;
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

    private ResponseEntity<VehicleResponse> reply(HttpStatus status, boolean ok, String message) {
        return ResponseEntity.status(status).body(new VehicleResponse(ok, message));
    }
}
