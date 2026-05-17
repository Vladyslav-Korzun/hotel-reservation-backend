package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

public record EmailAddress(String value) {

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new ValidationException("email is required");
        }
        value = value.trim().toLowerCase();
        if (!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("email format is invalid");
        }
    }
}
