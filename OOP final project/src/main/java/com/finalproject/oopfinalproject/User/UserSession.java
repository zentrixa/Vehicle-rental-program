package com.finalproject.oopfinalproject.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

/** Keeps track of who is logged in, using the server-side session. Only the username is stored. */
final class UserSession {

    private static final String USERNAME = "username";

    private UserSession() { }

    static void start(HttpServletRequest request, Account account) {
        if (request.getSession(false) != null) {
            request.changeSessionId(); // a fresh session ID on login stops session fixation
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(USERNAME, account.getUsername());
    }

    static Optional<Account> current(HttpServletRequest request, AccountRepository repository) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object username = session.getAttribute(USERNAME);
        if (username instanceof String name) {
            return repository.findByUsername(name);
        }
        return Optional.empty();
    }

    static void end(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
