package com.hotel.management.domain.reservation;

import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public final class ReservationAccessPolicy {

    public static final ReservationAccessPolicy INSTANCE = new ReservationAccessPolicy();

    private ReservationAccessPolicy() {
    }

    public void assertCanView(AuthenticatedUser actor, Reservation reservation) {
        if (actor != null && (actor.isAdmin() || actor.isStaff())) {
            return;
        }
        if (actor != null && actor.isGuest() && reservation != null && reservation.isOwnedBy(actor.guestId())) {
            return;
        }
        throw new ForbiddenException("Cannot view this reservation");
    }

    public void assertCanCancel(AuthenticatedUser actor, Reservation reservation) {
        if (actor != null && actor.isAdmin()) {
            return;
        }
        if (actor != null && actor.isGuest() && reservation != null && reservation.isOwnedBy(actor.guestId())) {
            return;
        }
        throw new ForbiddenException("Cannot cancel this reservation");
    }
}
