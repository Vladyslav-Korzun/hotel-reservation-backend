package com.hotel.management.domain.shared.exception;

public class ValidationException extends HotelReservationException {

    public ValidationException(String message) {
        super(message);
    }
}
