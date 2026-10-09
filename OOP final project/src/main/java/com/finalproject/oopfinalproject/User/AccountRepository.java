package com.finalproject.oopfinalproject.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Reads and writes accounts in the text-file database.
 *
 * Layout (under app.database.path, default src/main/Database):
 *   users/{username}.txt         one customer per file, one detail per line:
 *                                id, username, password, contact, licence, email, premium (true/false)
 *   admin/{username}.txt         one admin per file, the first line is the password
 *   GobalVar/UserNumber.txt      the ID the next customer will get
 *
 * File names are the lower-cased username, so "Zen" and "zen" are the same account.
 * Usernames are checked against a strict pattern before they touch the file system,
 * so a name like "../secret" can never escape the database folder.
 */
@Component
public class AccountRepository {

    private final Path usersDir;
    private final Path adminDir;
    private final Path counterFile;

    public AccountRepository(@Value("${app.database.path:src/main/Database}") String basePath) {
        Path base = Path.of(basePath);
        this.usersDir = base.resolve("users");
        this.adminDir = base.resolve("admin");
        this.counterFile = base.resolve("GobalVar").resolve("UserNumber.txt");
        try {
            Files.createDirectories(usersDir);
            Files.createDirectories(adminDir);
            Files.createDirectories(counterFile.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException("Can't prepare the database folders", e);
        }
    }

    // ---------- Create ----------

    /**
     * Saves a new customer and gives them the next ID.
     * Synchronized so two people registering at the same moment can't get the same ID or username.
     *
     * @throws DuplicateUsernameException if any account (customer or admin) already uses the username
     */
    public synchronized void add(Customer customer) throws DuplicateUsernameException {
        String key = fileKey(customer.getUsername());
        if (usernameTaken(key)) {
            throw new DuplicateUsernameException(customer.getUsername());
        }

        // take the ID first: if saving fails after this, an ID is skipped, never reused
        int id = readNextId();
        writeNextId(id + 1);
        customer.assignId(id);

        Path file = usersDir.resolve(key + ".txt");
        try {
            // CREATE_NEW fails if the file exists, which is the final safety net against duplicates
            Files.writeString(file, toRecord(customer), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException e) {
            throw new DuplicateUsernameException(customer.getUsername());
        } catch (IOException e) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // nothing more we can do; the original error is the one worth reporting
            }
            throw new UncheckedIOException("Couldn't save the account", e);
        }
    }

    // ---------- Update ----------

    /**
     * Saves changes to a customer that already exists. The new record is written to a temporary
     * file first and then moved over the old one, so a failed save can't leave a half-written file.
     */
    public synchronized void update(Customer customer) {
        Path file = usersDir.resolve(fileKey(customer.getUsername()) + ".txt");
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("There is no saved account for " + customer.getUsername());
        }
        Path temp = usersDir.resolve(fileKey(customer.getUsername()) + ".tmp");
        try {
            Files.writeString(temp, toRecord(customer), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // the original error is the one worth reporting
            }
            throw new UncheckedIOException("Couldn't save the changes", e);
        }
    }

    // ---------- Read ----------

    /** Finds an account by its exact username (admins first, then customers). */
    public Optional<Account> findByUsername(String username) {
        if (!Validation.isFileSafe(username)) {
            return Optional.empty();
        }
        String key = fileKey(username);

        Path adminFile = adminDir.resolve(key + ".txt");
        if (Files.isRegularFile(adminFile)) {
            Account admin = readAdmin(key, adminFile);
            return Optional.ofNullable(admin);
        }

        Path userFile = usersDir.resolve(key + ".txt");
        if (Files.isRegularFile(userFile)) {
            Account customer = readCustomer(userFile);
            return Optional.ofNullable(customer);
        }
        return Optional.empty();
    }

    /**
     * Finds the account a login attempt is for. The username is tried first; after that every
     * customer is asked whether the text is one of their login ids (a Premium member number, for example).
     */
    public Optional<Account> findByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return Optional.empty();
        }
        String id = loginId.trim();

        Optional<Account> byUsername = findByUsername(id);
        if (byUsername.isPresent()) {
            return byUsername;
        }

        for (Customer customer : allCustomers()) {
            if (customer.matchesLoginId(id)) {
                return Optional.of(customer);
            }
        }
        return Optional.empty();
    }

    /** Every customer, lowest member number first. */
    public List<Customer> findAllCustomers() {
        return allCustomers().stream()
                .sorted(Comparator.comparingInt(Customer::getId))
                .toList();
    }

    /**
     * Customers whose username contains the text (ignoring case) or whose member number is exactly
     * that number. A blank search returns everyone. The text is only compared in memory, never used
     * as a file name.
     */
    public List<Customer> searchCustomers(String query) {
        String text = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return findAllCustomers();
        }
        return findAllCustomers().stream()
                .filter(c -> c.getUsername().toLowerCase(Locale.ROOT).contains(text)
                        || String.valueOf(c.getId()).equals(text))
                .toList();
    }

    // ---------- Delete ----------

    /**
     * Removes the customer with this member number from the database. Only customer files are ever
     * touched, never admin files. IDs are not reused, so everyone else keeps theirs.
     *
     * @return the customer that was removed, or empty if nobody has that member number
     */
    public synchronized Optional<Customer> deleteCustomer(int id) {
        try (Stream<Path> files = Files.list(usersDir)) {
            for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".txt")).toList()) {
                Customer customer = readCustomer(file);
                if (customer != null && customer.getId() == id) {
                    Files.delete(file);
                    return Optional.of(customer);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't delete the customer", e);
        }
        return Optional.empty();
    }

    // ---------- Helpers ----------

    private static String fileKey(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    private boolean usernameTaken(String key) {
        return Files.exists(adminDir.resolve(key + ".txt")) || Files.exists(usersDir.resolve(key + ".txt"));
    }

    private static String toRecord(Customer customer) {
        return String.join("\n",
                String.valueOf(customer.getId()),
                customer.getUsername(),
                customer.passwordForStorage(),
                customer.getContactNo(),
                customer.getLicenceNo(),
                customer.getEmail(),
                String.valueOf(customer.isPremium()));
    }

    private Account readAdmin(String key, Path file) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return null;
            }
            return new Admin(key, lines.get(0));
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't read " + file, e);
        }
    }

    /** Returns null if the file isn't a valid customer record. */
    private Customer readCustomer(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.size() < 7) {
                return null;
            }
            int id = Integer.parseInt(lines.get(0).trim());
            boolean premium = "true".equalsIgnoreCase(lines.get(6).trim());
            return Customer.fromStorage(premium, id, lines.get(1), lines.get(2),
                    lines.get(3), lines.get(4), lines.get(5));
        } catch (NumberFormatException e) {
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't read " + file, e);
        }
    }

    private List<Customer> allCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Stream<Path> files = Files.list(usersDir)) {
            for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".txt")).toList()) {
                Customer customer = readCustomer(file);
                if (customer != null) {
                    customers.add(customer);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't list customers", e);
        }
        return customers;
    }

    private int readNextId() {
        try {
            if (Files.isRegularFile(counterFile)) {
                return Integer.parseInt(Files.readString(counterFile, StandardCharsets.UTF_8).trim());
            }
        } catch (NumberFormatException | IOException e) {
            // fall through and work the next ID out from the customers we already have
        }
        int highest = 0;
        for (Customer customer : allCustomers()) {
            highest = Math.max(highest, customer.getId());
        }
        return highest + 1;
    }

    private void writeNextId(int next) {
        try {
            Files.writeString(counterFile, String.valueOf(next), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't update the ID counter", e);
        }
    }
}
