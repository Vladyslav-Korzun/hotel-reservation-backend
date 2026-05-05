package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.util.List;

public record AccommodationParty(GuestComposition guests, List<PetDetails> pets) {

    public AccommodationParty {
        if (guests == null) {
            throw new ValidationException("guest composition is required");
        }
        pets = pets == null ? List.of() : List.copyOf(pets);
        pets.forEach(pet -> {
            if (pet == null) {
                throw new ValidationException("pets must not contain null values");
            }
        });
    }
}
