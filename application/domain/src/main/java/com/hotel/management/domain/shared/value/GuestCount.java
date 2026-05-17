package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

public record GuestCount(int adults, int children) {

    public GuestCount {
        if (adults < 1) {
            throw new ValidationException("adults must be at least 1");
        }
        if (children < 0) {
            throw new ValidationException("children must not be negative");
        }
    }

    public int total() {
        return adults + children;
    }
}
