package com.hotel.management.domain.predicate.hotel;

import com.hotel.management.domain.hotel.Hotel;

import java.util.function.Predicate;

public final class IsActiveHotelPredicate implements Predicate<Hotel> {

    public static final IsActiveHotelPredicate INSTANCE = new IsActiveHotelPredicate();

    private IsActiveHotelPredicate() {
    }

    @Override
    public boolean test(Hotel hotel) {
        return hotel != null && hotel.isActive();
    }
}
