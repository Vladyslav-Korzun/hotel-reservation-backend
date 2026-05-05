package com.hotel.management.service.availability;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.StayPeriod;

public record SearchAvailableRoomsCommand(
        String city,
        Long hotelId,
        StayPeriod stayPeriod,
        AccommodationParty accommodationParty
) {

    public SearchAvailableRoomsCommand {
        if (stayPeriod == null) {
            throw new ValidationException("stayPeriod is required");
        }
        if (accommodationParty == null) {
            throw new ValidationException("accommodationParty is required");
        }
        if (city != null) {
            city = city.trim();
        }
    }

    public static SearchAvailableRoomsCommand byCity(String city, StayPeriod stayPeriod, AccommodationParty accommodationParty) {
        return new SearchAvailableRoomsCommand(city, null, stayPeriod, accommodationParty);
    }

    public static SearchAvailableRoomsCommand byHotel(Long hotelId, StayPeriod stayPeriod, AccommodationParty accommodationParty) {
        return new SearchAvailableRoomsCommand(null, hotelId, stayPeriod, accommodationParty);
    }
}
