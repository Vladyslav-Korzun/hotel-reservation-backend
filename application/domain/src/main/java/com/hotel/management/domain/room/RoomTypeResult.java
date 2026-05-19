package com.hotel.management.domain.room;

import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetType;

import java.math.BigDecimal;
import java.util.Set;

public record RoomTypeResult(
        Long roomTypeId,
        Long hotelId,
        String name,
        int maxAdults,
        int maxChildren,
        int maxInfants,
        int maxTotalGuests,
        boolean petsAllowed,
        int maxPets,
        Set<PetType> allowedPetTypes,
        BigDecimal maxPetWeightKg,
        Money petFee,
        Money basePrice,
        String description,
        String bedSetup,
        BigDecimal roomSizeSqm,
        Set<RoomAmenity> amenities
) {
}
