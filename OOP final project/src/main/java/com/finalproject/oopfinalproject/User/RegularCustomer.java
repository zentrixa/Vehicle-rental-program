package com.finalproject.oopfinalproject.User;

/** A standard customer. Logs in with their username and password. */
public class RegularCustomer extends Customer {

    RegularCustomer(String username, String password, String contactNo, String licenceNo, String email) {
        super(username, password, contactNo, licenceNo, email);
    }

    RegularCustomer(int id, String username, String password, String contactNo, String licenceNo, String email) {
        super(id, username, password, contactNo, licenceNo, email);
    }

    @Override
    public boolean isPremium() {
        return false;
    }

    @Override
    public String getTierName() {
        return "Regular";
    }
}
