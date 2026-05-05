package com.hotel.management.service.exception;

import com.hotel.management.domain.shared.exception.HotelReservationException;

public class ForbiddenException extends HotelReservationException {

    public ForbiddenException(String message) {
        super(message);
    }
}
