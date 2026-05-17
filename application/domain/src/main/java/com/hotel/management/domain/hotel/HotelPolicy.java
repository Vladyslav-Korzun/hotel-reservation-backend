package com.hotel.management.domain.hotel;

import com.hotel.management.domain.shared.exception.ValidationException;

public record HotelPolicy(
        boolean childrenAllowed,
        boolean petsAllowed,
        int infantMaxAge,
        int childMaxAge,
        int adultEquivalentAge
) {

    private static final int MAX_MINOR_AGE = 17;

    public HotelPolicy {
        if (infantMaxAge < 0) {
            throw new ValidationException("infantMaxAge must not be negative");
        }
        if (childMaxAge < infantMaxAge) {
            throw new ValidationException("childMaxAge must be greater than or equal to infantMaxAge");
        }
        if (adultEquivalentAge <= infantMaxAge || adultEquivalentAge > MAX_MINOR_AGE) {
            throw new ValidationException("adultEquivalentAge must be greater than infantMaxAge and less than 18");
        }
    }

    public boolean isInfant(int age) {
        return age >= 0 && age <= infantMaxAge;
    }

    public boolean isChild(int age) {
        return age > infantMaxAge && age < adultEquivalentAge && age <= childMaxAge;
    }

    public boolean isAdultEquivalentMinor(int age) {
        return age >= adultEquivalentAge && age <= MAX_MINOR_AGE;
    }

    public boolean isSupportedMinorAge(int age) {
        return age >= 0 && age <= MAX_MINOR_AGE;
    }
}
