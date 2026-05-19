package com.hotel.management.domain.predicate;

import com.hotel.management.domain.predicate.reservation.CanCancelReservationPredicate;
import com.hotel.management.domain.predicate.reservation.IsActiveReservationPredicate;
import com.hotel.management.domain.predicate.room.FitsRoomCapacityPredicate;
import com.hotel.management.domain.predicate.serviceoffering.BelongsToHotelPredicate;
import com.hotel.management.domain.predicate.serviceoffering.IsActiveServiceOfferingPredicate;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationPriceSnapshot;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.GuestCount;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.RoomCapacity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdditionalDomainPredicateTest {

    @Test
    void shouldDetectReservationRules() {
        assertTrue(IsActiveReservationPredicate.INSTANCE.test(reservation(ReservationStatus.PENDING)));
        assertTrue(CanCancelReservationPredicate.INSTANCE.test(reservation(ReservationStatus.CONFIRMED)));
        assertFalse(CanCancelReservationPredicate.INSTANCE.test(reservation(ReservationStatus.CHECKED_OUT)));
    }

    @Test
    void shouldDetectRoomCapacityFit() {
        assertTrue(FitsRoomCapacityPredicate.INSTANCE.test(new GuestCount(2, 1), new RoomCapacity(3)));
        assertFalse(FitsRoomCapacityPredicate.INSTANCE.test(new GuestCount(2, 2), new RoomCapacity(3)));
    }

    @Test
    void shouldDetectServiceOfferingRules() {
        ServiceOffering offering = new ServiceOffering(
                1L,
                10L,
                "SPA",
                "Spa",
                null,
                Money.of("50.00", "EUR"),
                true,
                null
        );

        assertTrue(IsActiveServiceOfferingPredicate.INSTANCE.test(offering));
        assertTrue(BelongsToHotelPredicate.INSTANCE.test(offering, 10L));
        assertFalse(BelongsToHotelPredicate.INSTANCE.test(offering, 11L));
    }

    private static Reservation reservation(ReservationStatus status) {
        Long roomId = status == ReservationStatus.CHECKED_IN || status == ReservationStatus.CHECKED_OUT ? 100L : null;
        return Reservation.rehydrate(
                "reservation-1",
                1L,
                10L,
                roomId,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                new AccommodationParty(new GuestComposition(2, List.of()), List.of()),
                priceSnapshot(),
                List.of(),
                status,
                Instant.parse("2026-04-03T12:00:00Z"),
                status == ReservationStatus.CANCELLED ? Instant.parse("2026-04-04T12:00:00Z") : null,
                "guest-1"
        );
    }

    private static ReservationPriceSnapshot priceSnapshot() {
        Money zero = Money.zero("EUR");
        return new ReservationPriceSnapshot(zero, zero, zero, zero);
    }
}
