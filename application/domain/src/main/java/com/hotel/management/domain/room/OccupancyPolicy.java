package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;

public record OccupancyPolicy(
        int maxAdults,
        int maxChildren,
        int maxInfants,
        int maxTotalGuests
) {

    public OccupancyPolicy {
        if (maxAdults <= 0) {
            throw new ValidationException("maxAdults must be greater than zero");
        }
        if (maxChildren < 0) {
            throw new ValidationException("maxChildren must not be negative");
        }
        if (maxInfants < 0) {
            throw new ValidationException("maxInfants must not be negative");
        }
        if (maxTotalGuests <= 0) {
            throw new ValidationException("maxTotalGuests must be greater than zero");
        }
        if (maxTotalGuests < maxAdults) {
            throw new ValidationException("maxTotalGuests must be greater than or equal to maxAdults");
        }
    }
}
