package com.hotel.management.domain.service.availability;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record GetRoomTypeAvailabilityCalendarQuery(
        Long hotelId,
        Long roomTypeId,
        LocalDate from,
        LocalDate to
) {

    private static final int MAX_CALENDAR_DAYS = 90;

    public GetRoomTypeAvailabilityCalendarQuery {
        require(hotelId, "hotelId is required");
        require(roomTypeId, "roomTypeId is required");
        require(from, "from is required");
        require(to, "to is required");
        if (to.isBefore(from)) {
            throw new ValidationException("to must be on or after from");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_CALENDAR_DAYS) {
            throw new ValidationException("date range cannot exceed " + MAX_CALENDAR_DAYS + " days");
        }
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }
}
