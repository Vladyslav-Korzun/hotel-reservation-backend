package com.hotel.management.domain.serviceoffering;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceOfferingTest {

    @Test
    void shouldCreateAndToggleServiceOffering() {
        ServiceOffering breakfast = offering(true);

        assertTrue(breakfast.active());
        assertTrue(breakfast.belongsToHotel(1L));
        assertFalse(breakfast.deactivate().active());
        assertTrue(breakfast.deactivate().activate().active());
    }

    @Test
    void shouldRequireHotelCodeNameAndPrice() {
        assertThrows(ValidationException.class, () -> new ServiceOffering(
                1L,
                null,
                "BREAKFAST",
                "Breakfast",
                null,
                Money.of("15.00", "EUR"),
                true,
                null
        ));
    }

    private static ServiceOffering offering(boolean active) {
        return new ServiceOffering(
                1L,
                1L,
                "breakfast",
                "Breakfast",
                "Buffet breakfast",
                Money.of("15.00", "EUR"),
                active,
                null
        );
    }
}
