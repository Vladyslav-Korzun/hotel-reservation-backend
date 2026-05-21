package com.hotel.management.domain.staff;

import com.hotel.management.domain.shared.exception.ValidationException;

public final class Staff {

    private final Long id;
    private final String externalId;
    private final Long hotelId;

    public Staff(Long id, String externalId, Long hotelId) {
        this.id = id;
        this.externalId = requireText(externalId, "staff externalId is required");
        this.hotelId = hotelId;
    }

    public static Staff unassigned(String externalId) {
        return new Staff(null, externalId, null);
    }

    public Staff assignToHotel(Long hotelId) {
        if (hotelId == null) {
            throw new ValidationException("hotelId is required");
        }
        return new Staff(id, externalId, hotelId);
    }

    public boolean isAssigned() {
        return hotelId != null;
    }

    public Long id() {
        return id;
    }

    public String externalId() {
        return externalId;
    }

    public Long hotelId() {
        return hotelId;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
