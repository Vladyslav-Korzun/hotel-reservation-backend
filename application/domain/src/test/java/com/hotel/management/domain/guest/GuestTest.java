package com.hotel.management.domain.guest;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void shouldRegisterNewGuestFromKeycloakClaims() {
        Guest guest = Guest.register("kc-uuid-1", "alice@example.com", "Alice", "Smith");

        assertNull(guest.id());
        assertEquals("Alice", guest.firstName());
        assertEquals("Smith", guest.lastName());
        assertEquals("alice@example.com", guest.email().value());
        assertEquals("kc-uuid-1", guest.keycloakId());
    }

    @Test
    void shouldApplyFallbacksWhenClaimsAbsentOnRegister() {
        Guest guest = Guest.register("kc-uuid-2", null, null, null);

        assertNotNull(guest.email());
        assertEquals("kc-uuid-2@keycloak.local", guest.email().value());
        assertEquals("Guest", guest.firstName());
        assertEquals("User", guest.lastName());
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
