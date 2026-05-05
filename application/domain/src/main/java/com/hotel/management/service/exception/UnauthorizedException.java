package com.hotel.management.service.exception;

import com.hotel.management.domain.shared.exception.HotelReservationException;

public class UnauthorizedException extends HotelReservationException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
