package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;

public final class RoomType {

    private final Long id;
    private final Long hotelId;
    private final String name;
    private final OccupancyPolicy occupancyPolicy;
    private final PetPolicy petPolicy;
    private final Money basePrice;
    private final String description;

    public RoomType(
            Long id,
            Long hotelId,
            String name,
            OccupancyPolicy occupancyPolicy,
            PetPolicy petPolicy,
            Money basePrice,
            String description
    ) {
        this.id = require(id, "roomTypeId is required");
        this.hotelId = require(hotelId, "hotelId is required");
        this.name = requireText(name, "roomTypeName is required");
        this.occupancyPolicy = require(occupancyPolicy, "occupancyPolicy is required");
        this.petPolicy = require(petPolicy, "petPolicy is required");
        this.basePrice = require(basePrice, "basePrice is required");
        this.description = description;
    }

    public Long id() {
        return id;
    }

    public Long hotelId() {
        return hotelId;
    }

    public String name() {
        return name;
    }

    public OccupancyPolicy occupancyPolicy() {
        return occupancyPolicy;
    }

    public PetPolicy petPolicy() {
        return petPolicy;
    }

    public Money basePrice() {
        return basePrice;
    }

    public String description() {
        return description;
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
