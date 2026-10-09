package com.finalproject.oopfinalproject.User;

import java.util.regex.Pattern;

/**
 * The rules for customer details, in one place so every class applies the same ones.
 * Each method returns the cleaned value or throws an IllegalArgumentException whose
 * message is safe to show straight to the user.
 */
final class Validation {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,30}");
    private static final Pattern FILE_SAFE = Pattern.compile("[A-Za-z0-9_]{1,30}");
    private static final Pattern LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern CONTACT = Pattern.compile("\\+?[0-9]{7,15}");
    private static final Pattern LICENCE = Pattern.compile("[A-Za-z0-9]{5,20}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");

    private Validation() { }

    /** True if the text is safe to use as part of a file name (no dots, slashes or spaces). */
    static boolean isFileSafe(String text) {
        return text != null && FILE_SAFE.matcher(text).matches();
    }

    static String username(String value) {
        String text = clean(value);
        // at least one letter keeps usernames from being mistaken for member numbers
        if (!USERNAME.matcher(text).matches() || !LETTER.matcher(text).find()) {
            throw new IllegalArgumentException(
                    "Username must be 3 to 30 letters, numbers or underscores, with at least one letter.");
        }
        return text;
    }

    static String password(String value) {
        if (value == null || value.length() < 6 || value.length() > 100) {
            throw new IllegalArgumentException("Password must be 6 to 100 characters.");
        }
        if (CONTROL.matcher(value).find()) {
            throw new IllegalArgumentException("Password can't contain line breaks or control characters.");
        }
        return value;
    }

    static String contactNo(String value) {
        String text = clean(value);
        if (!CONTACT.matcher(text).matches()) {
            throw new IllegalArgumentException("Enter a contact number with 7 to 15 digits, no spaces.");
        }
        return text;
    }

    static String licenceNo(String value) {
        String text = clean(value);
        if (!LICENCE.matcher(text).matches()) {
            throw new IllegalArgumentException("Enter your licence number as printed, letters and digits only.");
        }
        return text;
    }

    static String email(String value) {
        String text = clean(value);
        if (text.length() > 100 || !EMAIL.matcher(text).matches()) {
            throw new IllegalArgumentException("Enter a valid email address, like name@example.com.");
        }
        return text;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
