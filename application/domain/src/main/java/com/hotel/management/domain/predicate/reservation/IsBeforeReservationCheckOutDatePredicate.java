package com.hotel.management.domain.predicate.reservation;

import com.hotel.management.domain.reservation.Reservation;

import java.time.LocalDate;
import java.util.function.BiPredicate;

public final class IsBeforeReservationCheckOutDatePredicate implements BiPredicate<Reservation, LocalDate> {

    public static final IsBeforeReservationCheckOutDatePredicate INSTANCE = new IsBeforeReservationCheckOutDatePredicate();

    private IsBeforeReservationCheckOutDatePredicate() {
    }

    @Override
    public boolean test(Reservation reservation, LocalDate today) {
        return reservation != null && today != null && today.isBefore(reservation.checkOut());
    }
}
