package com.hotel.management.domain.shared.exception;

public class ConflictException extends HotelReservationException {

    public ConflictException(String message) {
        super(message);
    }
}
