package com.hotel.management.service.hotel;

import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.domain.room.RoomAmenity;

import java.math.BigDecimal;
import java.util.Set;

public record CreateRoomTypeCommand(
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
        BigDecimal petFeeAmount,
        String petFeeCurrency,
        BigDecimal basePriceAmount,
        String basePriceCurrency,
        String description,
        String bedSetup,
        BigDecimal roomSizeSqm,
        Set<RoomAmenity> amenities
) {
}
