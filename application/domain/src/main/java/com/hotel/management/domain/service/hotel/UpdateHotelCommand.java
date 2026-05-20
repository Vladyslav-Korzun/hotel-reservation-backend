package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.shared.exception.ValidationException;

public record UpdateHotelCommand(
        Long hotelId,
        String name,
        String city,
        String country,
        String address,
        int stars,
        String description,
        HotelStatus status,
        boolean childrenAllowed,
        boolean petsAllowed,
        int infantMaxAge,
        int childMaxAge,
        int adultEquivalentAge
) {

    public UpdateHotelCommand {
        require(hotelId, "hotelId is required");
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }
}
