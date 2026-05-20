package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.shared.exception.ValidationException;

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

    public CreateRoomTypeCommand {
        require(roomTypeId, "roomTypeId is required");
        require(hotelId, "hotelId is required");
        allowedPetTypes = allowedPetTypes == null ? Set.of() : Set.copyOf(allowedPetTypes);
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }
}
