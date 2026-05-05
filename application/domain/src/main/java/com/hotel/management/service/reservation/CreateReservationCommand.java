package com.hotel.management.service.reservation;

import com.hotel.management.domain.shared.value.AccommodationParty;

import java.time.LocalDate;

public record CreateReservationCommand(
        Long hotelId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        AccommodationParty accommodationParty
) {
}
