package com.hotel.management.domain.predicate.serviceoffering;

import com.hotel.management.domain.serviceoffering.ServiceOffering;

import java.util.function.BiPredicate;

public final class BelongsToHotelPredicate implements BiPredicate<ServiceOffering, Long> {

    public static final BelongsToHotelPredicate INSTANCE = new BelongsToHotelPredicate();

    private BelongsToHotelPredicate() {
    }

    @Override
    public boolean test(ServiceOffering serviceOffering, Long hotelId) {
        return serviceOffering != null && hotelId != null && serviceOffering.belongsToHotel(hotelId);
    }
}
