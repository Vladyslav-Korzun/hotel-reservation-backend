package com.hotel.reservation.reservation.service;

import java.time.Instant;
import java.time.LocalDate;

public record GetReservationResult(
        String reservationId,
        Long hotelId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guestCount,
        String status,
        Instant createdAt,
        String createdBy
) {
}
