package com.hotel.management.domain.room;

import java.time.LocalDate;

public record RoomTypeAvailabilityCalendarDayResult(
        LocalDate date,
        int availableCount,
        boolean available
) {
}
