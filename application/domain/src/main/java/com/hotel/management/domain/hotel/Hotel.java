package com.hotel.management.domain.hotel;

import com.hotel.management.domain.shared.exception.ValidationException;

public final class Hotel {

    private final Long id;
    private final String name;
    private final String city;
    private final String country;
    private final String address;
    private final int stars;
    private final String description;
    private final HotelStatus status;
    private final HotelPolicy policy;

    public Hotel(
            Long id,
            String name,
            String city,
            String country,
            String address,
            int stars,
            String description,
            HotelStatus status,
            HotelPolicy policy
    ) {
        this.id = require(id, "hotelId is required");
        this.name = requireText(name, "hotelName is required");
        this.city = requireText(city, "city is required");
        this.country = requireText(country, "country is required");
        this.address = requireText(address, "address is required");
        if (stars < 1 || stars > 5) {
            throw new ValidationException("hotel stars must be between 1 and 5");
        }
        this.stars = stars;
        this.description = description;
        this.status = require(status, "hotelStatus is required");
        this.policy = require(policy, "hotelPolicy is required");
    }

    public boolean isActive() {
        return status == HotelStatus.ACTIVE;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String city() {
        return city;
    }

    public String country() {
        return country;
    }

    public String address() {
        return address;
    }

    public int stars() {
        return stars;
    }

    public String description() {
        return description;
    }

    public HotelStatus status() {
        return status;
    }

    public HotelPolicy policy() {
        return policy;
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
