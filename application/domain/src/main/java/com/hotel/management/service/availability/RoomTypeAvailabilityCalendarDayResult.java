package com.hotel.management.service.availability;

import java.time.LocalDate;

public record RoomTypeAvailabilityCalendarDayResult(
        LocalDate date,
        int availableCount,
        boolean available
) {
}
