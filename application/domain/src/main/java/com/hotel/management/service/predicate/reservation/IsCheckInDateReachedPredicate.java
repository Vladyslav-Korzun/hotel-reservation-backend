package com.hotel.management.service.predicate.reservation;

import com.hotel.management.domain.reservation.Reservation;

import java.time.LocalDate;
import java.util.function.BiPredicate;

public final class IsCheckInDateReachedPredicate implements BiPredicate<Reservation, LocalDate> {

    public static final IsCheckInDateReachedPredicate INSTANCE = new IsCheckInDateReachedPredicate();

    private IsCheckInDateReachedPredicate() {
    }

    @Override
    public boolean test(Reservation reservation, LocalDate today) {
        return reservation != null && today != null && !today.isBefore(reservation.checkIn());
    }
}
