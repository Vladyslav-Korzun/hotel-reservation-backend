package com.hotel.management.service.reservation;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.EmailAddress;

public record GuestContactCommand(
        String firstName,
        String lastName,
        String email,
        String phone
) {

    public GuestContactCommand {
        firstName = requireText(firstName, "firstName is required");
        lastName = requireText(lastName, "lastName is required");
        email = new EmailAddress(email).value();
        phone = requireText(phone, "phone is required");
    }

    public EmailAddress emailAddress() {
        return new EmailAddress(email);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
