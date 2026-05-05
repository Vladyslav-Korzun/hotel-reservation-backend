package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetType;

import java.math.BigDecimal;
import java.util.Set;

public record PetPolicy(
        boolean petsAllowed,
        int maxPets,
        Set<PetType> allowedPetTypes,
        BigDecimal maxPetWeightKg,
        Money petFee
) {

    public PetPolicy {
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
    }
}
