package com.hotel.management.domain.reservation;

import com.hotel.management.domain.shared.value.Money;

public record ReservationServiceItemResult(
        Long serviceOfferingId,
        String serviceName,
        Money price,
        int quantity,
        Money totalPrice
) {
}
