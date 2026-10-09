package com.finalproject.oopfinalproject.User;

/** One row of the admin customer list. Deliberately has no password. */
public record CustomerSummary(int id, String username, String email, String contactNo,
                              String licenceNo, String membership) {

    public static CustomerSummary of(Customer customer) {
        return new CustomerSummary(customer.getId(), customer.getUsername(), customer.getEmail(),
                customer.getContactNo(), customer.getLicenceNo(), customer.getTierName());
    }
}
