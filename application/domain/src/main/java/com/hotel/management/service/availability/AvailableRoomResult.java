package com.hotel.management.service.availability;

import com.hotel.management.domain.shared.value.Money;

public record AvailableRoomResult(
        Long hotelId,
        String hotelName,
        Long roomTypeId,
        String roomTypeName,
        int maxAdults,
        int maxChildren,
        int maxInfants,
        int maxTotalGuests,
        boolean petsAllowed,
        int maxPets,
        Money basePrice,
        int availableCount
) {
}
