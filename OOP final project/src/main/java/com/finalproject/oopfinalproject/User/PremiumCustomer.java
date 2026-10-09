package com.finalproject.oopfinalproject.User;

/**
 * A premium customer. Same as a regular customer, except they can also log in with
 * their member number instead of their username (this is the polymorphic part:
 * the login code just calls matchesLoginId and each type answers for itself).
 */
public class PremiumCustomer extends Customer {

    PremiumCustomer(String username, String password, String contactNo, String licenceNo, String email) {
        super(username, password, contactNo, licenceNo, email);
    }

    PremiumCustomer(int id, String username, String password, String contactNo, String licenceNo, String email) {
        super(id, username, password, contactNo, licenceNo, email);
    }

    @Override
    public boolean isPremium() {
        return true;
    }

    @Override
    public String getTierName() {
        return "Premium";
    }

    @Override
    public boolean matchesLoginId(String loginId) {
        return super.matchesLoginId(loginId)
                || (loginId != null && String.valueOf(getId()).equals(loginId.trim()));
    }
}
