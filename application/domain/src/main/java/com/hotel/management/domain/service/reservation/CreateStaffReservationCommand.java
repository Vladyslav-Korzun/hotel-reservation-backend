package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;

import java.time.LocalDate;
import java.util.List;

public record CreateStaffReservationCommand(
        Long hotelId,
        Long roomTypeId,
        Long guestId,
        GuestContactCommand guestContact,
        LocalDate checkIn,
        LocalDate checkOut,
        AccommodationParty accommodationParty,
        List<ServiceOfferingSelection> serviceOfferings
) {

    public CreateStaffReservationCommand {
        require(hotelId, "hotelId is required");
        require(roomTypeId, "roomTypeId is required");
        if (guestId == null && guestContact == null) {
            throw new ValidationException("guest contact is required");
        }
        serviceOfferings = serviceOfferings == null ? List.of() : List.copyOf(serviceOfferings);
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
