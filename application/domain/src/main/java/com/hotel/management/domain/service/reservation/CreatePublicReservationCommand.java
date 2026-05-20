package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;

import java.time.LocalDate;
import java.util.List;

public record CreatePublicReservationCommand(
        Long hotelId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        AccommodationParty accommodationParty,
        List<ServiceOfferingSelection> serviceOfferings,
        GuestContactCommand guestContact
) {

    public CreatePublicReservationCommand {
        require(hotelId, "hotelId is required");
        require(roomTypeId, "roomTypeId is required");
        require(checkIn, "checkIn is required");
        require(checkOut, "checkOut is required");
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("checkOut must be after checkIn");
        }
        require(accommodationParty, "accommodationParty is required");
        serviceOfferings = serviceOfferings == null ? List.of() : List.copyOf(serviceOfferings);
        require(guestContact, "guest contact is required");
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    public CreateReservationCommand toReservationCommand() {
        return new CreateReservationCommand(
                hotelId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                null,
                null,
                null,
                serviceOfferings
        );
    }
}
