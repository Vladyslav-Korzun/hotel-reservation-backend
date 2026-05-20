package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.shared.exception.ValidationException;

public record DeactivateServiceOfferingCommand(Long hotelId, Long serviceOfferingId) {

    public DeactivateServiceOfferingCommand {
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
