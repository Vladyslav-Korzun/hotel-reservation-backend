package com.hotel.management.domain.staff;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.util.Objects;

public final class Staff {

    private final Long id;
    private final String externalId;
    private final Long hotelId;
    /** Preferred username from Keycloak JWT — populated on first login, nullable for legacy records. */
    private final String username;
    /** Email from Keycloak JWT — populated on first login, nullable for legacy records. */
    private final String email;

    /** Full constructor. */
    public Staff(Long id, String externalId, Long hotelId, String username, String email) {
        this.id = id;
        this.externalId = requireText(externalId, "staff externalId is required");
        this.hotelId = hotelId;
        this.username = username;
        this.email = email;
    }

    /** Backward-compatible constructor (username/email unknown). */
    public Staff(Long id, String externalId, Long hotelId) {
        this(id, externalId, hotelId, null, null);
    }

    public static Staff unassigned(String externalId) {
        return new Staff(null, externalId, null, null, null);
    }

    public Staff assignToHotel(Long hotelId) {
        if (hotelId == null) {
            throw new ValidationException("hotelId is required");
        }
        return new Staff(id, externalId, hotelId, username, email);
    }

    public Staff unassignFromHotel() {
        return new Staff(id, externalId, null, username, email);
    }

    /** Returns a copy of this staff record with updated display-name fields. */
    public Staff withDisplayName(String username, String email) {
        return new Staff(id, externalId, hotelId, username, email);
    }

    /**
     * Returns a copy with updated display-name fields, or {@code this} if nothing changed.
     * Callers can detect a change by reference: {@code updated != this}.
     */
    public Staff withUpdatedDisplayName(String username, String email) {
        if (Objects.equals(this.username, username) && Objects.equals(this.email, email)) {
            return this;
        }
        return new Staff(id, externalId, hotelId, username, email);
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

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
