package com.hotel.management.domain.guest;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuestTest {

    @Test
    void shouldCreateGuestWithContactDetails() {
        Guest guest = new Guest(1L, "John", "Smith", new EmailAddress("john@example.com"), " +421 900 000 000 ");

        assertEquals(1L, guest.id());
        assertEquals("John", guest.firstName());
        assertEquals("+421 900 000 000", guest.phone());
    }

    @Test
    void shouldAllowMissingPhoneAndTransientIdentity() {
        Guest guest = new Guest(1L, "John", "Smith", new EmailAddress("john@example.com"), null);
        Guest transientGuest = new Guest(null, "Jane", "Smith", new EmailAddress("jane@example.com"), null);

        assertNull(guest.phone());
        assertNull(transientGuest.id());
        assertThrows(ValidationException.class, () -> new Guest(1L, " ", "Smith", new EmailAddress("john@example.com"), null));
    }
}
