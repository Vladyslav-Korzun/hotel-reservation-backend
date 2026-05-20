package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.math.BigDecimal;

public record CreateServiceOfferingCommand(
        Long hotelId,
        Long serviceOfferingId,
        String code,
        String name,
        String description,
        BigDecimal priceAmount,
        String priceCurrency,
        boolean active,
        String availabilityRule
) {

    public CreateServiceOfferingCommand {
        require(hotelId, "hotelId is required");
        require(serviceOfferingId, "serviceOfferingId is required");
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }
}
