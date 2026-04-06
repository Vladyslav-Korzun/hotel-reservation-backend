package com.hotel.reservation.shared.exception;

public class UnauthorizedException extends HotelReservationException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
