package com.hotel.management.domain.reservation;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;

public record ReservationPriceSnapshot(
        Money basePrice,
        Money servicesPrice,
        Money discountAmount,
        Money finalPrice
) {

    public ReservationPriceSnapshot {
        if (basePrice == null) {
            throw new ValidationException("basePrice is required");
        }
        if (servicesPrice == null) {
            throw new ValidationException("servicesPrice is required");
        }
        if (discountAmount == null) {
            throw new ValidationException("discountAmount is required");
        }
        if (finalPrice == null) {
            throw new ValidationException("finalPrice is required");
        }
    }
}
