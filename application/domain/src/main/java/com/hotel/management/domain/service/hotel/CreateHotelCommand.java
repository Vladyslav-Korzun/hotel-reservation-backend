package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.shared.exception.ValidationException;

public record CreateHotelCommand(
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

    public CreateHotelCommand {
        require(hotelId, "hotelId is required");
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }
}
