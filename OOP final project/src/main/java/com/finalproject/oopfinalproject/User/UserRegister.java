package com.finalproject.oopfinalproject.User;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UncheckedIOException;

/** Handles registration: validates the details, saves the customer and logs them in. */
@RestController
public class UserRegister {

    private final AccountRepository repository;

    public UserRegister(AccountRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/api/signup")
    public ResponseEntity<AuthResponse> signup(@RequestParam(defaultValue = "") String name,
                                               @RequestParam(defaultValue = "") String password,
                                               @RequestParam(defaultValue = "") String contactNo,
                                               @RequestParam(defaultValue = "") String licenceNo,
                                               @RequestParam(defaultValue = "") String email,
                                               @RequestParam(defaultValue = "false") String membership,
                                               HttpServletRequest request) {

        if (name.isBlank() || password.isEmpty() || contactNo.isBlank()
                || licenceNo.isBlank() || email.isBlank()) {
            return reply(HttpStatus.BAD_REQUEST, "Fill in every field.");
        }

        Customer customer;
        try {
            // the factory picks Premium or Regular and validates every detail
            customer = Customer.register("true".equalsIgnoreCase(membership.trim()),
                    name, password, contactNo, licenceNo, email);
        } catch (IllegalArgumentException e) {
            return reply(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try {
            repository.add(customer);
        } catch (DuplicateUsernameException e) {
            return reply(HttpStatus.CONFLICT, "That username is taken. Choose another.");
        } catch (UncheckedIOException e) {
            e.printStackTrace();
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, "We couldn't save your account. Try again.");
        }

        UserSession.start(request, customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.success(customer.getHomePage()));
    }

    private static ResponseEntity<AuthResponse> reply(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(AuthResponse.error(message));
    }
}
