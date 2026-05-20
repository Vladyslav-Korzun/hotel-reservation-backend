package com.hotel.management.domain.predicate.reservation;

import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationPriceSnapshot;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.hotel.management.domain.predicate.reservation.IsBeforeReservationCheckOutDatePredicate;
import com.hotel.management.domain.predicate.reservation.IsCheckInDateReachedPredicate;

class ReservationPredicateTest {

    @Test
    void shouldDetectAllowedCheckInDates() {
        Reservation reservation = reservation();

        assertFalse(IsCheckInDateReachedPredicate.INSTANCE.test(reservation, LocalDate.parse("2026-05-09")));
        assertTrue(IsCheckInDateReachedPredicate.INSTANCE.test(reservation, LocalDate.parse("2026-05-10")));
        assertTrue(IsBeforeReservationCheckOutDatePredicate.INSTANCE.test(reservation, LocalDate.parse("2026-05-11")));
        assertFalse(IsBeforeReservationCheckOutDatePredicate.INSTANCE.test(reservation, LocalDate.parse("2026-05-12")));
    }

    private static Reservation reservation() {
        return Reservation.rehydrate(
                "reservation-1",
                1L,
                10L,
                null,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                new AccommodationParty(new GuestComposition(2, List.of()), List.of()),
                priceSnapshot(),
                List.of(),
                ReservationStatus.PENDING,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        );
    }

    private static ReservationPriceSnapshot priceSnapshot() {
        Money zero = Money.zero("EUR");
        return new ReservationPriceSnapshot(zero, zero, zero, zero);
    }
}
