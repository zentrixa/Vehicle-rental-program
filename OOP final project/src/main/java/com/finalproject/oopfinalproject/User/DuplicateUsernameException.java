package com.finalproject.oopfinalproject.User;

/** Thrown when someone tries to register a username that already belongs to an account. */
public class DuplicateUsernameException extends Exception {

    public DuplicateUsernameException(String username) {
        super("The username \"" + username + "\" is already taken");
    }
}
