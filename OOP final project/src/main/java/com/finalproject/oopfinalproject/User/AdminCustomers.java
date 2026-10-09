package com.finalproject.oopfinalproject.User;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

/**
 * The admin dashboard's data: list or search customers, and delete one.
 * Every request is checked on the server, so hiding the page in the browser isn't what keeps
 * customers out; they get a 403 here.
 */
@RestController
public class AdminCustomers {

    private final AccountRepository repository;

    public AdminCustomers(AccountRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/admin/customers")
    public ResponseEntity<List<CustomerSummary>> list(@RequestParam(defaultValue = "") String q,
                                                      HttpServletRequest request) {
        Optional<HttpStatus> denied = denial(request);
        if (denied.isPresent()) {
            return ResponseEntity.status(denied.get()).build();
        }

        List<CustomerSummary> customers = repository.searchCustomers(q).stream()
                .map(CustomerSummary::of)
                .toList();
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(customers);
    }

    @DeleteMapping("/api/admin/customers/{id}")
    public ResponseEntity<AuthResponse> delete(@PathVariable int id, HttpServletRequest request) {
        Optional<HttpStatus> denied = denial(request);
        if (denied.isPresent()) {
            return ResponseEntity.status(denied.get()).body(AuthResponse.error(
                    denied.get() == HttpStatus.UNAUTHORIZED ? "Log in as an admin to do this." : "Only admins can do this."));
        }

        try {
            Optional<Customer> removed = repository.deleteCustomer(id);
            if (removed.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(AuthResponse.error("That customer no longer exists."));
            }
            return ResponseEntity.ok(AuthResponse.done("Deleted " + removed.get().getUsername() + "."));
        } catch (UncheckedIOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("We couldn't delete that customer. Try again."));
        }
    }

    /** Empty if the caller is a logged-in admin; otherwise the status to refuse them with. */
    private Optional<HttpStatus> denial(HttpServletRequest request) {
        Optional<Account> account = UserSession.current(request, repository);
        if (account.isEmpty()) {
            return Optional.of(HttpStatus.UNAUTHORIZED);
        }
        if (!(account.get() instanceof Admin)) {
            return Optional.of(HttpStatus.FORBIDDEN);
        }
        return Optional.empty();
    }
}
