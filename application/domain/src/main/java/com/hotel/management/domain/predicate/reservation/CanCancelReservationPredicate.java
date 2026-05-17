package com.hotel.management.domain.predicate.reservation;

import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationStatus;

import java.util.function.Predicate;

public final class CanCancelReservationPredicate implements Predicate<Reservation> {

    public static final CanCancelReservationPredicate INSTANCE = new CanCancelReservationPredicate();

    private CanCancelReservationPredicate() {
    }

    @Override
    public boolean test(Reservation reservation) {
        return reservation != null
                && reservation.status() != ReservationStatus.CANCELLED
                && reservation.status() != ReservationStatus.CHECKED_IN
                && reservation.status() != ReservationStatus.CHECKED_OUT
                && reservation.status() != ReservationStatus.NO_SHOW;
    }
}
