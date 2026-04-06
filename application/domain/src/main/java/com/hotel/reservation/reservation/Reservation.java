package com.hotel.reservation.reservation;

import java.time.Instant;
import java.time.LocalDate;

public record Reservation(
        String id,
        Long hotelId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guestCount,
        ReservationStatus status,
        Instant createdAt,
        String createdBy
) {
}
