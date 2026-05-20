package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.util.List;

public record AccommodationParty(GuestComposition guests, List<PetDetails> pets, List<StayingGuest> stayingGuests) {

    private static final int ADULT_MIN_AGE = 18;

    public AccommodationParty(GuestComposition guests, List<PetDetails> pets) {
        this(guests, pets, List.of());
    }

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
        stayingGuests = stayingGuests == null ? List.of() : copyStayingGuests(stayingGuests);
    }

    public static AccommodationParty fromComposition(GuestComposition guests, List<PetDetails> pets) {
        return new AccommodationParty(guests, pets);
    }

    public static AccommodationParty fromStayingGuests(List<StayingGuest> stayingGuests, List<PetDetails> pets) {
        List<StayingGuest> normalizedGuests = normalizeStayingGuests(stayingGuests);
        int adults = (int) normalizedGuests.stream()
                .filter(guest -> guest.age() >= ADULT_MIN_AGE)
                .count();
        if (adults == 0) {
            throw new ValidationException("at least one adult staying guest is required");
        }
        List<Integer> childrenAges = normalizedGuests.stream()
                .filter(guest -> guest.age() < ADULT_MIN_AGE)
                .map(StayingGuest::age)
                .toList();
        return new AccommodationParty(new GuestComposition(adults, childrenAges), pets, normalizedGuests);
    }

    private static List<StayingGuest> normalizeStayingGuests(List<StayingGuest> stayingGuests) {
        if (stayingGuests == null || stayingGuests.isEmpty()) {
            throw new ValidationException("at least one staying guest is required");
        }
        return copyStayingGuests(stayingGuests);
    }

    private static List<StayingGuest> copyStayingGuests(List<StayingGuest> stayingGuests) {
        stayingGuests.forEach(stayingGuest -> {
            if (stayingGuest == null) {
                throw new ValidationException("stayingGuests must not contain null values");
            }
        });
        return List.copyOf(stayingGuests);
    }
}
