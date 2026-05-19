package com.hotel.management.domain.hotel;

import com.hotel.management.domain.shared.value.Money;

public record HotelServiceOfferingResult(
        Long serviceOfferingId,
        Long hotelId,
        String code,
        String name,
        String description,
        Money price,
        boolean active,
        String availabilityRule
) {
}
