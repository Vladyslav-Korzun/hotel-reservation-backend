package com.hotel.management.domain.stay;

import com.hotel.management.domain.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StayTest {

    @Test
    void shouldStartAndCompleteStay() {
        var checkedInAt = Instant.parse("2026-05-10T10:00:00Z");
        var checkedOutAt = Instant.parse("2026-05-12T10:00:00Z");

        var stay = Stay.start(null, "reservation-1", 100L, checkedInAt);

        assertNull(stay.id());
        assertEquals("reservation-1", stay.reservationId());
        assertEquals(StayStatus.ACTIVE, stay.status());
        assertNull(stay.checkedOutAt());

        var completedStay = stay.complete(checkedOutAt);

        assertEquals(StayStatus.COMPLETED, completedStay.status());
        assertEquals(checkedOutAt, completedStay.checkedOutAt());
    }

    @Test
    void shouldRejectInvalidStayState() {
        assertThrows(ValidationException.class, () -> Stay.start(null, " ", 100L, Instant.parse("2026-05-10T10:00:00Z")));
        assertThrows(ValidationException.class, () -> Stay.start(null, "reservation-1", null, Instant.parse("2026-05-10T10:00:00Z")));
        assertThrows(ValidationException.class, () -> Stay.rehydrate(
                1L,
                "reservation-1",
                100L,
                Instant.parse("2026-05-10T10:00:00Z"),
                null,
                StayStatus.COMPLETED
        ));
        assertThrows(ValidationException.class, () -> Stay.rehydrate(
                1L,
                "reservation-1",
                100L,
                Instant.parse("2026-05-12T10:00:00Z"),
                Instant.parse("2026-05-10T10:00:00Z"),
                StayStatus.COMPLETED
        ));
    }
}
