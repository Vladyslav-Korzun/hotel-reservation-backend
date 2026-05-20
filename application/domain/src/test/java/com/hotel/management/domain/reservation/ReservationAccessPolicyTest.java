package com.hotel.management.domain.reservation;

import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationAccessPolicyTest {

    @Test
    void shouldAllowAdminAndStaffToViewAnyReservation() {
        Reservation reservation = reservation("guest-123");

        assertDoesNotThrow(() -> ReservationAccessPolicy.INSTANCE.assertCanView(user("admin-1", "ADMIN"), reservation));
        assertDoesNotThrow(() -> ReservationAccessPolicy.INSTANCE.assertCanView(user("staff-1", "STAFF"), reservation));
    }

    @Test
    void shouldAllowGuestToViewOwnReservation() {
        Reservation reservation = reservation("guest-123");

        assertDoesNotThrow(() -> ReservationAccessPolicy.INSTANCE.assertCanView(user("guest-123", "GUEST"), reservation));
    }

    @Test
    void shouldRejectGuestViewingAnotherReservation() {
        Reservation reservation = reservation("guest-123");

        assertThrows(
                ForbiddenException.class,
                () -> ReservationAccessPolicy.INSTANCE.assertCanView(user("guest-999", "GUEST"), reservation)
        );
    }

    @Test
    void shouldAllowAdminAndOwnerToCancelReservation() {
        Reservation reservation = reservation("guest-123");

        assertDoesNotThrow(() -> ReservationAccessPolicy.INSTANCE.assertCanCancel(user("admin-1", "ADMIN"), reservation));
        assertDoesNotThrow(() -> ReservationAccessPolicy.INSTANCE.assertCanCancel(user("guest-123", "GUEST"), reservation));
    }

    @Test
    void shouldRejectStaffAndNonOwnerCancellingReservation() {
        Reservation reservation = reservation("guest-123");

        assertThrows(
                ForbiddenException.class,
                () -> ReservationAccessPolicy.INSTANCE.assertCanCancel(user("staff-1", "STAFF"), reservation)
        );
        assertThrows(
                ForbiddenException.class,
                () -> ReservationAccessPolicy.INSTANCE.assertCanCancel(user("guest-999", "GUEST"), reservation)
        );
    }

    private static AuthenticatedUser user(String id, String role) {
        return new AuthenticatedUser(id, Set.of(role));
    }

    private static Reservation reservation(String createdBy) {
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
                createdBy
        );
    }

    private static ReservationPriceSnapshot priceSnapshot() {
        Money zero = Money.zero("EUR");
        return new ReservationPriceSnapshot(zero, zero, zero, zero);
    }
}
