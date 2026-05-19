package com.hotel.management.domain.reservation;

public record ActiveReservationView(
        Long hotelId,
        Long roomTypeId
) {
}
