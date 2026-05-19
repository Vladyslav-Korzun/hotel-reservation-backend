package com.hotel.management.domain.predicate.reservation;

import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

import java.util.function.BiPredicate;

public final class IsReservationOwnerPredicate implements BiPredicate<Reservation, AuthenticatedUser> {

    public static final IsReservationOwnerPredicate INSTANCE = new IsReservationOwnerPredicate();

    private IsReservationOwnerPredicate() {
    }

    @Override
    public boolean test(Reservation reservation, AuthenticatedUser user) {
        return reservation != null && user != null && reservation.belongsTo(user.userId());
    }
}