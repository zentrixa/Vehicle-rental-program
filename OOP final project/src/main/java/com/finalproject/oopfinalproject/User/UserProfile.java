package com.finalproject.oopfinalproject.User;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UncheckedIOException;
import java.util.Optional;

/** Who am I (used by the listing, profile and admin pages), change my details, and log out. */
@RestController
public class UserProfile {

    private final AccountRepository repository;

    public UserProfile(AccountRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/me")
    public ResponseEntity<MeResponse> me(HttpServletRequest request) {
        Optional<Account> account = UserSession.current(request, repository);
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(MeResponse.of(account.get()));
    }

    /**
     * Updates the logged-in customer's details. The username and member number never change.
     * A new password is only accepted together with the correct current password.
     */
    @PostMapping("/api/profile")
    public ResponseEntity<AuthResponse> update(@RequestParam(defaultValue = "") String email,
                                               @RequestParam(defaultValue = "") String contactNo,
                                               @RequestParam(defaultValue = "") String licenceNo,
                                               @RequestParam(defaultValue = "") String membership,
                                               @RequestParam(defaultValue = "") String currentPassword,
                                               @RequestParam(defaultValue = "") String newPassword,
                                               HttpServletRequest request) {

        Optional<Account> account = UserSession.current(request, repository);
        if (account.isEmpty()) {
            return reply(HttpStatus.UNAUTHORIZED, "Log in to change your details.");
        }
        if (!(account.get() instanceof Customer customer)) {
            return reply(HttpStatus.FORBIDDEN, "Admin accounts can't be edited here.");
        }

        Customer updated;
        try {
            // each setter checks its own value, so nothing invalid gets as far as the file
            customer.setEmail(email);
            customer.setContactNo(contactNo);
            customer.setLicenceNo(licenceNo);

            if (!newPassword.isEmpty() && !customer.changePassword(currentPassword, newPassword)) {
                return reply(HttpStatus.FORBIDDEN, "Your current password is wrong.");
            }

            // a blank membership means "leave it as it is"
            boolean premium = membership.isBlank()
                    ? customer.isPremium()
                    : "true".equalsIgnoreCase(membership.trim());
            updated = customer.withPremium(premium);
        } catch (IllegalArgumentException e) {
            return reply(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        try {
            repository.update(updated);
        } catch (UncheckedIOException e) {
            e.printStackTrace();
            return reply(HttpStatus.INTERNAL_SERVER_ERROR, "We couldn't save your changes. Try again.");
        }
        return ResponseEntity.ok(AuthResponse.done("Details updated."));
    }

    private static ResponseEntity<AuthResponse> reply(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(AuthResponse.error(message));
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        UserSession.end(request);
        return ResponseEntity.noContent().build();
    }
}
