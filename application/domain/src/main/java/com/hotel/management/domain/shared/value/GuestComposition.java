package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.util.List;

public record GuestComposition(int adults, List<Integer> childrenAges) {

    public GuestComposition {
        if (adults <= 0) {
            throw new ValidationException("At least one adult is required");
        }
        childrenAges = childrenAges == null ? List.of() : List.copyOf(childrenAges);
        childrenAges.forEach(age -> {
            if (age == null) {
                throw new ValidationException("childrenAges must not contain null values");
            }
            if (age < 0) {
                throw new ValidationException("child age must not be negative");
            }
        });
    }
}
