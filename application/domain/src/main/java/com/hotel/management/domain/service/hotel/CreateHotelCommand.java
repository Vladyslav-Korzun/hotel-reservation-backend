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
        requireText(name, "hotelName is required");
        requireText(city, "city is required");
        requireText(country, "country is required");
        requireText(address, "address is required");
        if (stars < 1 || stars > 5) {
            throw new ValidationException("hotel stars must be between 1 and 5");
        }
        require(status, "hotelStatus is required");
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value;
    }
}
