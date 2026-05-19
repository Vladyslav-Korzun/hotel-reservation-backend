package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.shared.value.AccommodationParty;

import java.time.LocalDate;
import java.util.List;

record ResolvedCreateReservationCommand(
        Long hotelId,
        Long roomTypeId,
        Long guestId,
        LocalDate checkIn,
        LocalDate checkOut,
        AccommodationParty accommodationParty,
        String contactEmail,
        String contactPhone,
        String specialRequests,
        List<ServiceOfferingSelection> serviceOfferings
) {

    ResolvedCreateReservationCommand {
        serviceOfferings = serviceOfferings == null ? List.of() : List.copyOf(serviceOfferings);
    }

    static ResolvedCreateReservationCommand from(CreateReservationCommand command, Long guestId) {
        return new ResolvedCreateReservationCommand(
                command.hotelId(),
                command.roomTypeId(),
                guestId,
                command.checkIn(),
                command.checkOut(),
                command.accommodationParty(),
                command.contactEmail(),
                command.contactPhone(),
                command.specialRequests(),
                command.serviceOfferings()
        );
    }
}
