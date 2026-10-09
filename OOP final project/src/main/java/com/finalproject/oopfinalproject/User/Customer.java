package com.finalproject.oopfinalproject.User;

import java.util.List;

/**
 * A customer of the rental platform. Abstract: every customer is either a
 * {@link RegularCustomer} or a {@link PremiumCustomer}.
 *
 * Fields are private. Setters check their input, so a Customer can never hold
 * details that break the rules in {@link Validation}.
 */
public abstract class Customer extends Account {

    private static final int NO_ID = -1;

    private int id = NO_ID;
    private String contactNo;
    private String licenceNo;
    private String email;

    /** For a brand-new registration: every detail is validated. */
    Customer(String username, String password, String contactNo, String licenceNo, String email) {
        super(Validation.username(username), Validation.password(password));
        setContactNo(contactNo);
        setLicenceNo(licenceNo);
        setEmail(email);
    }

    /** For rebuilding a customer already saved on disk: the stored values are trusted as they are. */
    Customer(int id, String username, String password, String contactNo, String licenceNo, String email) {
        super(username, password);
        this.id = id;
        this.contactNo = contactNo;
        this.licenceNo = licenceNo;
        this.email = email;
    }

    /** Creates a validated customer of the right type. Throws IllegalArgumentException on bad input. */
    public static Customer register(boolean premium, String username, String password,
                                    String contactNo, String licenceNo, String email) {
        return premium
                ? new PremiumCustomer(username, password, contactNo, licenceNo, email)
                : new RegularCustomer(username, password, contactNo, licenceNo, email);
    }

    /** Rebuilds a customer from stored data. Only the storage class (same package) uses this. */
    static Customer fromStorage(boolean premium, int id, String username, String password,
                                String contactNo, String licenceNo, String email) {
        return premium
                ? new PremiumCustomer(id, username, password, contactNo, licenceNo, email)
                : new RegularCustomer(id, username, password, contactNo, licenceNo, email);
    }

    public abstract boolean isPremium();

    public abstract String getTierName();

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public String getHomePage() {
        return "/listingpage/listingpage.html";
    }

    @Override
    public List<ProfileField> getProfileFields() {
        return List.of(
                new ProfileField("id", "Member number", String.valueOf(id)),
                new ProfileField("username", "Username", getUsername()),
                new ProfileField("email", "Email", email),
                new ProfileField("contactNo", "Contact number", contactNo),
                new ProfileField("licenceNo", "Licence number", licenceNo),
                new ProfileField("membership", "Membership", getTierName()));
    }

    /**
     * Changes the password after checking the current one.
     *
     * @return false if the current password was wrong
     * @throws IllegalArgumentException if the new password breaks the password rules
     */
    public boolean changePassword(String current, String newPassword) {
        return replacePassword(current, Validation.password(newPassword));
    }

    /**
     * A customer can't change class, so switching tier builds the matching object
     * (Regular or Premium) with the same details and member number.
     */
    public Customer withPremium(boolean premium) {
        if (premium == isPremium()) {
            return this;
        }
        return fromStorage(premium, id, getUsername(), passwordForStorage(), contactNo, licenceNo, email);
    }

    public int getId() {
        return id;
    }

    /** Set once, by the storage class, when the customer is saved. */
    void assignId(int newId) {
        if (id != NO_ID) {
            throw new IllegalStateException("This customer already has an ID");
        }
        id = newId;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = Validation.contactNo(contactNo);
    }

    public String getLicenceNo() {
        return licenceNo;
    }

    public void setLicenceNo(String licenceNo) {
        this.licenceNo = Validation.licenceNo(licenceNo);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Validation.email(email);
    }
}
