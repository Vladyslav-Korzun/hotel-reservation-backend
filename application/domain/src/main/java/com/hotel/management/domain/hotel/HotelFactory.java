package com.hotel.management.domain.hotel;

public class HotelFactory {

    public Hotel create(
            Long hotelId,
            String name,
            String city,
            String country,
            String address,
            int stars,
            String description,
            HotelStatus status,
            HotelPolicy policy
    ) {
        return new Hotel(hotelId, name, city, country, address, stars, description, status, policy);
    }
}
