package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

public record RoomCapacity(int value) {

    public RoomCapacity {
        if (value <= 0) {
            throw new ValidationException("room capacity must be greater than zero");
        }
    }
}
