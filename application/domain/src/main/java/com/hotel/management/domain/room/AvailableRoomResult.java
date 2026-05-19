package com.hotel.management.domain.room;

import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.shared.value.Money;

import java.math.BigDecimal;
import java.util.Set;

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
        int availableCount,
        String bedSetup,
        BigDecimal roomSizeSqm,
        Set<RoomAmenity> amenities
) {
}
