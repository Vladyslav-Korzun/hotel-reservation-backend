package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.shared.value.AccommodationParty;

import java.time.LocalDate;
import java.util.List;

public record CreateReservationCommand(
        Long hotelId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        AccommodationParty accommodationParty,
        String contactEmail,
        String contactPhone,
        String specialRequests,
        List<ServiceOfferingSelection> serviceOfferings
) {

    public CreateReservationCommand {
        serviceOfferings = serviceOfferings == null ? List.of() : List.copyOf(serviceOfferings);
    }

    public CreateReservationCommand(
            Long hotelId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty
    ) {
        this(hotelId, roomTypeId, checkIn, checkOut, accommodationParty, null, null, null, List.of());
    }

    public CreateReservationCommand(
            Long hotelId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            List<ServiceOfferingSelection> serviceOfferings
    ) {
        this(hotelId, roomTypeId, checkIn, checkOut, accommodationParty, null, null, null, serviceOfferings);
    }
}
