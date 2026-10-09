package com.finalproject.oopfinalproject.User;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * Handles login for customers and admins alike. This class never checks what type of account
 * it found: the account answers authenticate() and getHomePage() for itself.
 */
@RestController
public class UserLogin {

    private final AccountRepository repository;

    public UserLogin(AccountRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/api/login")
    public ResponseEntity<AuthResponse> login(@RequestParam(defaultValue = "") String name,
                                              @RequestParam(defaultValue = "") String password,
                                              HttpServletRequest request) {

        String loginId = name.trim();
        if (loginId.isEmpty() || password.isEmpty()) {
            return reply(HttpStatus.BAD_REQUEST, "Enter your username and password.");
        }

        Optional<Account> found;
        try {
            found = repository.findByLoginId(loginId);
        } catch (UncheckedIOException e) {
            e.printStackTrace();
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, "We couldn't log you in. Try again.");
        }

        if (found.isEmpty()) {
            return reply(HttpStatus.NOT_FOUND, "That user doesn't exist.");
        }

        Account account = found.get();
        if (!account.authenticate(loginId, password)) {
            return reply(HttpStatus.UNAUTHORIZED, "Wrong password. Try again.");
        }

        UserSession.start(request, account);
        return ResponseEntity.ok(AuthResponse.success(account.getHomePage()));
    }

    private static ResponseEntity<AuthResponse> reply(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(AuthResponse.error(message));
    }
}
