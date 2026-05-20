package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

public record StayingGuest(String firstName, String lastName, Integer age, GuestGender gender) {

    private static final int MAX_AGE = 130;
    private static final int MAX_NAME_LENGTH = 120;

    public StayingGuest {
        firstName = requireText(firstName, "firstName is required");
        lastName = requireText(lastName, "lastName is required");
        if (age == null) {
            throw new ValidationException("guest age is required");
        }
        if (age < 0 || age > MAX_AGE) {
            throw new ValidationException("guest age must be between 0 and 130");
        }
        if (gender == null) {
            throw new ValidationException("gender is required");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new ValidationException(message.replace(" is required", " must not exceed 120 characters"));
        }
        return trimmed;
    }
}
