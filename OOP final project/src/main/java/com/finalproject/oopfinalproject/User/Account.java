package com.finalproject.oopfinalproject.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Anyone who can log in: a customer or an admin.
 *
 * The password is private and never handed out. The only things the outside world can do
 * with it are check an attempt ({@link #authenticate}) or, inside this package, let the
 * storage class write it to disk.
 *
 * Subclasses decide how they behave by overriding the abstract methods and
 * {@link #matchesLoginId}, so the login code never needs to ask "what type are you?".
 */
public abstract class Account {

    private final String username;
    private String password;

    protected Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    /** "CUSTOMER" or "ADMIN". */
    public abstract String getRole();

    /** The page this kind of account lands on after registering or logging in. */
    public abstract String getHomePage();

    /** What this kind of account shows on its profile page. */
    public abstract List<ProfileField> getProfileFields();

    /**
     * Does this text identify this account at login? By default only the username does.
     * Subclasses can accept more (see PremiumCustomer).
     */
    public boolean matchesLoginId(String loginId) {
        return loginId != null && username.equalsIgnoreCase(loginId.trim());
    }

    /** Template method: the shared password check, with each subclass supplying its own login id rule. */
    public final boolean authenticate(String loginId, String attempt) {
        return matchesLoginId(loginId) && passwordMatches(attempt);
    }

    /**
     * Swaps in a new password if (and only if) the current one is right. Subclasses wrap this
     * with their own rules about what a good password looks like.
     */
    protected final boolean replacePassword(String current, String newPassword) {
        if (!passwordMatches(current)) {
            return false;
        }
        this.password = newPassword;
        return true;
    }

    private boolean passwordMatches(String attempt) {
        if (attempt == null) {
            return false;
        }
        // MessageDigest.isEqual compares in constant time
        return MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                attempt.getBytes(StandardCharsets.UTF_8));
    }

    /** Package-private on purpose: only AccountRepository (same package) needs the raw password. */
    String passwordForStorage() {
        return password;
    }
}
