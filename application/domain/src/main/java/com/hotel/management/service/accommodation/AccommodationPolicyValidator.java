package com.hotel.management.service.accommodation;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.PetDetails;

public class AccommodationPolicyValidator {

    public void validate(Hotel hotel, RoomType roomType, AccommodationParty accommodationParty) {
        require(hotel, "hotel is required");
        require(roomType, "roomType is required");
        require(accommodationParty, "accommodationParty is required");

        HotelPolicy hotelPolicy = hotel.policy();
        OccupancyPolicy occupancyPolicy = roomType.occupancyPolicy();
        PetPolicy petPolicy = roomType.petPolicy();

        if (!hotelPolicy.childrenAllowed() && !accommodationParty.guests().childrenAges().isEmpty()) {
            throw new ValidationException("Children are not allowed in this hotel");
        }

        int infants = 0;
        int children = 0;
        for (Integer age : accommodationParty.guests().childrenAges()) {
            if (hotelPolicy.isInfant(age)) {
                infants++;
                continue;
            }
            if (hotelPolicy.isChild(age)) {
                children++;
                continue;
            }
            throw new ValidationException("Child age " + age + " is outside supported child range");
        }

        if (accommodationParty.guests().adults() > occupancyPolicy.maxAdults()) {
            throw new ValidationException("adults exceed room type limit");
        }
        if (children > occupancyPolicy.maxChildren()) {
            throw new ValidationException("children exceed room type limit");
        }
        if (infants > occupancyPolicy.maxInfants()) {
            throw new ValidationException("infants exceed room type limit");
        }
        if (accommodationParty.guests().adults() + children > occupancyPolicy.maxTotalGuests()) {
            throw new ValidationException("guest occupancy exceeds room type limit");
        }

        validatePets(hotelPolicy, petPolicy, accommodationParty);
    }

    public int chargeableGuestCount(HotelPolicy hotelPolicy, AccommodationParty accommodationParty) {
        require(hotelPolicy, "hotelPolicy is required");
        require(accommodationParty, "accommodationParty is required");

        int children = 0;
        for (Integer age : accommodationParty.guests().childrenAges()) {
            if (hotelPolicy.isChild(age)) {
                children++;
            } else if (!hotelPolicy.isInfant(age)) {
                throw new ValidationException("Child age " + age + " is outside supported child range");
            }
        }
        return accommodationParty.guests().adults() + children;
    }

    private void validatePets(HotelPolicy hotelPolicy, PetPolicy petPolicy, AccommodationParty accommodationParty) {
        if (accommodationParty.pets().isEmpty()) {
            return;
        }
        if (!hotelPolicy.petsAllowed()) {
            throw new ValidationException("Pets are not allowed in this hotel");
        }
        if (!petPolicy.petsAllowed()) {
            throw new ValidationException("Pets are not allowed for this room type");
        }
        if (accommodationParty.pets().size() > petPolicy.maxPets()) {
            throw new ValidationException("pets exceed room type limit");
        }
        for (PetDetails pet : accommodationParty.pets()) {
            if (!petPolicy.allowedPetTypes().contains(pet.type())) {
                throw new ValidationException("Pet type " + pet.type() + " is not allowed");
            }
            if (petPolicy.maxPetWeightKg() != null && pet.weightKg().compareTo(petPolicy.maxPetWeightKg()) > 0) {
                throw new ValidationException("Pet weight exceeds room type limit");
            }
        }
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }
}
