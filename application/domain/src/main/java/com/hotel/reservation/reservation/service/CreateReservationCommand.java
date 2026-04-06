package com.hotel.reservation.reservation.service;

import java.time.LocalDate;

public record CreateReservationCommand(
        Long hotelId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guestCount
) {
}
