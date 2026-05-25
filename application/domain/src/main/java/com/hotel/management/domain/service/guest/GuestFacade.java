package com.hotel.management.domain.service.guest;

import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface GuestFacade {

    /**
     * Resolves the guest record for the authenticated user.
     * Finds an existing guest by Keycloak UUID, binds a legacy account on first login,
     * or auto-creates a new guest from JWT claims.
     *
     * @param actor authenticated user with GUEST role — subject, guestId, email, firstName, lastName are read from it
     * @return the resolved Guest domain entity
     */
    Guest resolveGuest(AuthenticatedUser actor);
}
