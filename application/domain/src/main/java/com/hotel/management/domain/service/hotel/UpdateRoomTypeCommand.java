package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.PetType;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateRoomTypeCommand(
        Long roomTypeId,
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

    public UpdateRoomTypeCommand {
        require(roomTypeId, "roomTypeId is required");
        requireText(name, "roomTypeName is required");
        if (maxAdults <= 0) {
            throw new ValidationException("maxAdults must be greater than zero");
        }
        if (maxChildren < 0) {
            throw new ValidationException("maxChildren must not be negative");
        }
        if (maxInfants < 0) {
            throw new ValidationException("maxInfants must not be negative");
        }
        if (maxTotalGuests <= 0) {
            throw new ValidationException("maxTotalGuests must be greater than zero");
        }
        if (maxTotalGuests < maxAdults) {
            throw new ValidationException("maxTotalGuests must be greater than or equal to maxAdults");
        }
        if (maxPets < 0) {
            throw new ValidationException("maxPets must not be negative");
        }
        allowedPetTypes = allowedPetTypes == null ? Set.of() : Set.copyOf(allowedPetTypes);
        if (maxPetWeightKg != null && maxPetWeightKg.signum() < 0) {
            throw new ValidationException("maxPetWeightKg must not be negative");
        }
        if (!petsAllowed && maxPets > 0) {
            throw new ValidationException("maxPets must be zero when pets are not allowed");
        }
        if (!petsAllowed && !allowedPetTypes.isEmpty()) {
            throw new ValidationException("allowedPetTypes must be empty when pets are not allowed");
        }
        requireMoney(basePriceAmount, basePriceCurrency, "basePrice");
        if (roomSizeSqm != null && (roomSizeSqm.signum() <= 0 || roomSizeSqm.compareTo(new BigDecimal("1000")) > 0)) {
            throw new ValidationException("roomSizeSqm must be between 1 and 1000");
        }
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value;
    }

    private static void requireMoney(BigDecimal amount, String currencyCode, String fieldName) {
        if (amount == null) {
            throw new ValidationException(fieldName + " amount is required");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new ValidationException(fieldName + " currency is required");
        }
    }
}
