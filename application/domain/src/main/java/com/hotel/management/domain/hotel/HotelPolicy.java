package com.hotel.management.domain.hotel;

import com.hotel.management.domain.shared.exception.ValidationException;

public record HotelPolicy(
        boolean childrenAllowed,
        boolean petsAllowed,
        int infantMaxAge,
        int childMaxAge
) {

    public HotelPolicy {
        if (infantMaxAge < 0) {
            throw new ValidationException("infantMaxAge must not be negative");
        }
        if (childMaxAge < infantMaxAge) {
            throw new ValidationException("childMaxAge must be greater than or equal to infantMaxAge");
        }
    }

    public boolean isInfant(int age) {
        return age >= 0 && age <= infantMaxAge;
    }

    public boolean isChild(int age) {
        return age > infantMaxAge && age <= childMaxAge;
    }
}
