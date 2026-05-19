package com.hotel.management.domain.hotel;

public record HotelResult(
        Long hotelId,
        String name,
        String city,
        String country,
        String address,
        int stars,
        String description,
        String status,
        boolean childrenAllowed,
        boolean petsAllowed,
        int infantMaxAge,
        int childMaxAge,
        int adultEquivalentAge
) {
}
