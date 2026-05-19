package com.hotel.management.domain.shared.security;

import com.hotel.management.domain.shared.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticatedUserTest {

    @Test
    void shouldRequireAdminRole() {
        new AuthenticatedUser("admin-1", Set.of("ADMIN")).requireAdmin();

        assertThrows(ForbiddenException.class, () -> new AuthenticatedUser("guest-1", Set.of("GUEST")).requireAdmin());
    }

    @Test
    void shouldRequireStaffOrAdminRole() {
        new AuthenticatedUser("staff-1", Set.of("STAFF")).requireStaffOrAdmin();
        new AuthenticatedUser("admin-1", Set.of("ADMIN")).requireStaffOrAdmin();

        assertThrows(ForbiddenException.class, () -> new AuthenticatedUser("guest-1", Set.of("GUEST")).requireStaffOrAdmin());
    }

    @Test
    void shouldRequireStaffRole() {
        new AuthenticatedUser("staff-1", Set.of("STAFF")).requireStaff();

        assertThrows(ForbiddenException.class, () -> new AuthenticatedUser("admin-1", Set.of("ADMIN")).requireStaff());
    }

    @Test
    void shouldRequireGuestRole() {
        new AuthenticatedUser("guest-1", Set.of("GUEST")).requireGuest();

        assertThrows(ForbiddenException.class, () -> new AuthenticatedUser("staff-1", Set.of("STAFF")).requireGuest());
    }

    @Test
    void shouldRequireGuestId() {
        var guest = new AuthenticatedUser("guest-1", Set.of("GUEST"), 42L);

        assertEquals(42L, guest.requireGuestId());
        assertThrows(ForbiddenException.class, () -> new AuthenticatedUser("guest-2", Set.of("GUEST")).requireGuestId());
        assertThrows(ForbiddenException.class, () -> new AuthenticatedUser("staff-1", Set.of("STAFF")).requireGuestId());
    }
}
