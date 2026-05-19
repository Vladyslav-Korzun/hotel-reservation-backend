package com.hotel.management.domain.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationServiceItemTest {

    @Test
    void shouldSnapshotActiveServiceOffering() {
        ReservationServiceItem item = ReservationServiceItem.snapshot("reservation-1", offering(true), 2);

        assertEquals("reservation-1", item.reservationId());
        assertEquals("Breakfast", item.serviceNameSnapshot());
        assertEquals(Money.of("30.00", "EUR"), item.totalPrice());
    }

    @Test
    void shouldRejectInactiveServiceOffering() {
        assertThrows(ValidationException.class, () -> ReservationServiceItem.snapshot("reservation-1", offering(false), 1));
        assertThrows(ValidationException.class, () -> ReservationServiceItem.snapshot("reservation-1", offering(true), 0));
    }

    private static ServiceOffering offering(boolean active) {
        return new ServiceOffering(
                1L,
                1L,
                "BREAKFAST",
                "Breakfast",
                "Buffet breakfast",
                Money.of("15.00", "EUR"),
                active,
                null
        );
    }
}
