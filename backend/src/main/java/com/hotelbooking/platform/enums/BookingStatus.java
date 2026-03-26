package com.hotelbooking.platform.enums;

public enum BookingStatus {

    /**
     * Booking is currently active.
     */
    ACTIVE("ACTIVE"),

    /**
     * Booking was confirmed.
     */
    CONFIRMED("CONFIRMED"),

    /**
     * Guest check-in.
     */
    CHECKED_IN("CHECKED_IN"),

    /**
     * Booking has been cancelled.
     */
    CANCELLED("CANCELLED"),

    /**
     * Booking has been completed (e.g. after checkout).
     */
    COMPLETED("COMPLETED");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    /**
     * Returns the string value used by the service layer (e.g. "ACTIVE").
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Map a string status (for example, the one stored in the database or used
     * in BookingService) to the corresponding {@link BookingStatus}.
     *
     * @param value string representation of the status (e.g. "ACTIVE")
     * @return matching {@link BookingStatus}
     * @throws IllegalArgumentException if the value does not match any status
     */
    public static BookingStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("BookingStatus value cannot be null");
        }
        for (BookingStatus status : BookingStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown BookingStatus value: " + value);
    }
}
