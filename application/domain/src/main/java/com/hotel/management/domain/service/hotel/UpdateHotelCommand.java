package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.hotel.HotelStatus;

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
}
