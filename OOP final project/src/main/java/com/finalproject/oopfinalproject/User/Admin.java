package com.finalproject.oopfinalproject.User;

import java.util.List;

/** An administrator. Admins can't register; their accounts are created by hand in the admin folder. */
public class Admin extends Account {

    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getHomePage() {
        return "/admin/admin.html";
    }

    @Override
    public List<ProfileField> getProfileFields() {
        return List.of(
                new ProfileField("username", "Username", getUsername()),
                new ProfileField("role", "Role", "Administrator"));
    }
}
