package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.math.BigDecimal;

public record UpdateServiceOfferingCommand(
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

    public UpdateServiceOfferingCommand {
        require(hotelId, "hotelId is required");
        require(serviceOfferingId, "serviceOfferingId is required");
        requireText(code, "serviceOfferingCode is required");
        requireText(name, "serviceOfferingName is required");
        requireMoney(priceAmount, priceCurrency, "serviceOfferingPrice");
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

    private static void requireMoney(BigDecimal amount, String currencyCode, String fieldName) {
        if (amount == null) {
            throw new ValidationException(fieldName + " amount is required");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new ValidationException(fieldName + " currency is required");
        }
    }
}
