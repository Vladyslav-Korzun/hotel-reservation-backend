package com.hotel.management.domain.shared.exception;

public class NotFoundException extends HotelReservationException {

    public NotFoundException(String message) {
        super(message);
    }
}
