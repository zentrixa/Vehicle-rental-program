package com.finalproject.oopfinalproject.User;

import java.util.List;

/** What /api/me sends back about the logged-in account. Never includes the password. */
public record MeResponse(String username, String role, String homePage, List<ProfileField> fields) {

    public static MeResponse of(Account account) {
        return new MeResponse(account.getUsername(), account.getRole(),
                account.getHomePage(), account.getProfileFields());
    }
}
