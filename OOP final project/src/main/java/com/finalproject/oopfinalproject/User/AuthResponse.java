package com.finalproject.oopfinalproject.User;

/** What /api/signup and /api/login send back: success with a page to go to, or a message to show. */
public record AuthResponse(boolean ok, String message, String redirect) {

    public static AuthResponse success(String redirect) {
        return new AuthResponse(true, null, redirect);
    }

    /** Success with nothing to redirect to, just a message to show. */
    public static AuthResponse done(String message) {
        return new AuthResponse(true, message, null);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, null);
    }
}
