package com.hotel.management.service.reservation;

public record ActiveReservationView(
        Long hotelId,
        Long roomTypeId
) {
}
