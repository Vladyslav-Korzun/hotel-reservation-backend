package com.hotel.management.domain.guest;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.EmailAddress;

public final class Guest {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final EmailAddress email;
    private final String phone;
    private final String keycloakId;

    public Guest(Long id, String firstName, String lastName, EmailAddress email, String phone, String keycloakId) {
        this.id = id;
        this.firstName = requireText(firstName, "firstName is required");
        this.lastName = requireText(lastName, "lastName is required");
        this.email = require(email, "email is required");
        this.phone = normalizePhone(phone);
        this.keycloakId = keycloakId;
    }

    public Guest(Long id, String firstName, String lastName, EmailAddress email, String phone) {
        this(id, firstName, lastName, email, phone, null);
    }

    public Long id() {
        return id;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public EmailAddress email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public String keycloakId() {
        return keycloakId;
    }

    public static Guest register(String keycloakId, String email, String firstName, String lastName) {
        String resolvedEmail = email     != null ? email     : keycloakId + "@keycloak.local";
        String resolvedFirst = firstName != null ? firstName : "Guest";
        String resolvedLast  = lastName  != null ? lastName  : "User";
        return new Guest(null, resolvedFirst, resolvedLast, new EmailAddress(resolvedEmail), null, keycloakId);
    }

    public Guest withKeycloakId(String keycloakId) {
        return new Guest(this.id, this.firstName, this.lastName, this.email, this.phone, keycloakId);
    }

    private static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
