package com.hotel.management.domain.shared.exception;

public class ForbiddenException extends HotelReservationException {

    public ForbiddenException(String message) {
        super(message);
    }
}
