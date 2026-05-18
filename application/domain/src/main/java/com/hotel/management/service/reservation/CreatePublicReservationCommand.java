package com.hotel.management.service.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
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
        serviceOfferings = serviceOfferings == null ? List.of() : List.copyOf(serviceOfferings);
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
