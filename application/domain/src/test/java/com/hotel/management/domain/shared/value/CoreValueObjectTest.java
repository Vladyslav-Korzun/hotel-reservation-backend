package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreValueObjectTest {

    @Test
    void shouldNormalizeAndValidateEmail() {
        assertEquals("guest@example.com", new EmailAddress(" Guest@Example.com ").value());
        assertThrows(ValidationException.class, () -> new EmailAddress("invalid-email"));
    }

    @Test
    void shouldValidateGuestCountAndRoomCapacity() {
        GuestCount guestCount = new GuestCount(2, 1);

        assertEquals(3, guestCount.total());
        assertThrows(ValidationException.class, () -> new GuestCount(0, 1));
        assertThrows(ValidationException.class, () -> new RoomCapacity(0));
    }

    @Test
    void shouldValidateDateRangeAndDetectOverlap() {
        DateRange first = new DateRange(LocalDate.parse("2026-05-10"), LocalDate.parse("2026-05-12"));
        DateRange second = new DateRange(LocalDate.parse("2026-05-11"), LocalDate.parse("2026-05-13"));

        assertTrue(first.overlaps(second));
        assertThrows(ValidationException.class, () -> new DateRange(
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-10")
        ));
    }

    @Test
    void shouldCalculateMoneyWithSameCurrency() {
        Money base = Money.of("100.00", "EUR");

        assertEquals(Money.of("300.00", "EUR"), base.multiply(3));
        assertEquals(Money.of("150.00", "EUR"), base.plus(Money.of("50.00", "EUR")));
        assertThrows(ValidationException.class, () -> base.plus(Money.of("50.00", "USD")));
    }
}
