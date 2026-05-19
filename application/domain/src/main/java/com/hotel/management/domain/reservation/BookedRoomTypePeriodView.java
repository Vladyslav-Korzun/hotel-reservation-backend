package com.hotel.management.domain.reservation;

import java.time.LocalDate;

public record BookedRoomTypePeriodView(
        String reservationId,
        LocalDate checkIn,
        LocalDate checkOut
) {
}
