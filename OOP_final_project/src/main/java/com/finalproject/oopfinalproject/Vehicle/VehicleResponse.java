package com.finalproject.oopfinalproject.Vehicle;

public class VehicleResponse {
    private boolean ok;
    private String message;

    public VehicleResponse(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }
}
