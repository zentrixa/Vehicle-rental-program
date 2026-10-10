package com.vehiclerental.vehicle;

/**
 * MEMBER 2 - The only allowed states of a vehicle.
 * An enum is a special class with a fixed set of objects, each with its own label.
 */
public enum VehicleStatus {

    AVAILABLE("Available"),
    RENTED("On rent"),
    MAINTENANCE("In maintenance");

    private final String label;

    VehicleStatus(String label) {           // enum constructor
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Converts user text such as "rented" into VehicleStatus.RENTED. */
    public static VehicleStatus fromText(String text) {
        for (VehicleStatus s : values()) {
            if (s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new com.vehiclerental.common.ValidationException("Unknown vehicle status: " + text);
    }
}
