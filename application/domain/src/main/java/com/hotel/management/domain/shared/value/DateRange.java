package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.time.LocalDate;

public record DateRange(LocalDate from, LocalDate to) {

    public DateRange {
        if (from == null) {
            throw new ValidationException("from is required");
        }
        if (to == null) {
            throw new ValidationException("to is required");
        }
        if (!to.isAfter(from)) {
            throw new ValidationException("to must be after from");
        }
    }

    public boolean overlaps(DateRange other) {
        if (other == null) {
            throw new ValidationException("dateRange is required");
        }
        return from.isBefore(other.to) && to.isAfter(other.from);
    }
}
