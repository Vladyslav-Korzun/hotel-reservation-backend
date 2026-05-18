package com.hotel.management.service.hotel;

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
}
