package com.hotel.management.service.reservation;

import java.time.LocalDate;

public record BookedRoomTypePeriodView(
        String reservationId,
        LocalDate checkIn,
        LocalDate checkOut
) {
}
