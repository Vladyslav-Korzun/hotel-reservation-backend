package com.hotel.management.service.exception;

import com.hotel.management.domain.shared.exception.HotelReservationException;

public class ConflictException extends HotelReservationException {

    public ConflictException(String message) {
        super(message);
    }
}
