package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.math.BigDecimal;

public record PetDetails(PetType type, PetSize size, BigDecimal weightKg) {

    public PetDetails {
        if (type == null) {
            throw new ValidationException("pet type is required");
        }
        if (size == null) {
            throw new ValidationException("pet size is required");
        }
        if (weightKg == null) {
            throw new ValidationException("pet weight is required");
        }
        if (weightKg.signum() < 0) {
            throw new ValidationException("pet weight must not be negative");
        }
    }
}
