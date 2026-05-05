package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.time.LocalDate;

public record StayPeriod(LocalDate checkIn, LocalDate checkOut) {

    public StayPeriod {
        if (checkIn == null) {
            throw new ValidationException("checkIn is required");
        }
        if (checkOut == null) {
            throw new ValidationException("checkOut is required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("checkOut must be after checkIn");
        }
    }

    public boolean overlaps(StayPeriod other) {
        if (other == null) {
            throw new ValidationException("stayPeriod is required");
        }
        return checkIn.isBefore(other.checkOut) && checkOut.isAfter(other.checkIn);
    }
}
