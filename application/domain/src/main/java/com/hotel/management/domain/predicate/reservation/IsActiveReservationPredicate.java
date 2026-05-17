package com.hotel.management.domain.predicate.reservation;

import com.hotel.management.domain.reservation.Reservation;

import java.util.function.Predicate;

public final class IsActiveReservationPredicate implements Predicate<Reservation> {

    public static final IsActiveReservationPredicate INSTANCE = new IsActiveReservationPredicate();

    private IsActiveReservationPredicate() {
    }

    @Override
    public boolean test(Reservation reservation) {
        return reservation != null && reservation.isActiveForAvailability();
    }
}
