package com.hotel.management.domain.reservation;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationFactoryTest {

    private final ReservationFactory factory = new ReservationFactory();

    @Test
    void shouldCreatePendingReservationForGuest() {
        Reservation reservation = factory.createPendingReservation(
                "reservation-1",
                1L,
                10L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                priceSnapshot(),
                List.of(),
                Instant.parse("2026-04-03T12:00:00Z"),
                "guest-123"
        );

        assertEquals("reservation-1", reservation.id());
        assertEquals(10L, reservation.guestId());
        assertEquals(ReservationStatus.PENDING, reservation.status());
        assertEquals("guest-123", reservation.createdBy());
        assertEquals(Instant.parse("2026-04-03T12:00:00Z"), reservation.createdAt());
    }

    @Test
    void shouldRejectPendingReservationWithoutOwner() {
        assertThrows(ValidationException.class, () -> factory.createPendingReservation(
                "reservation-1",
                1L,
                10L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                priceSnapshot(),
                List.of(),
                Instant.parse("2026-04-03T12:00:00Z"),
                null
        ));
    }

    private static AccommodationParty party(int adults) {
        return new AccommodationParty(new GuestComposition(adults, List.of()), List.of());
    }

    private static ReservationPriceSnapshot priceSnapshot() {
        var zero = com.hotel.management.domain.shared.value.Money.zero("EUR");
        return new ReservationPriceSnapshot(zero, zero, zero, zero);
    }
}
