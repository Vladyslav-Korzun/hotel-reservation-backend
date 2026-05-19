package com.hotel.management.domain.shared.exception;

public class UnauthorizedException extends HotelReservationException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
