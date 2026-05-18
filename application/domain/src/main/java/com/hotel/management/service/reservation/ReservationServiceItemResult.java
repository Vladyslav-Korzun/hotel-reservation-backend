package com.hotel.management.service.reservation;

import com.hotel.management.domain.shared.value.Money;

public record ReservationServiceItemResult(
        Long serviceOfferingId,
        String serviceName,
        Money price,
        int quantity,
        Money totalPrice
) {
}
