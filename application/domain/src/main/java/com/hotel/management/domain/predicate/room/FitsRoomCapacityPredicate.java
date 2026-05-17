package com.hotel.management.domain.predicate.room;

import com.hotel.management.domain.shared.value.GuestCount;
import com.hotel.management.domain.shared.value.RoomCapacity;

import java.util.function.BiPredicate;

public final class FitsRoomCapacityPredicate implements BiPredicate<GuestCount, RoomCapacity> {

    public static final FitsRoomCapacityPredicate INSTANCE = new FitsRoomCapacityPredicate();

    private FitsRoomCapacityPredicate() {
    }

    @Override
    public boolean test(GuestCount guestCount, RoomCapacity roomCapacity) {
        return guestCount != null && roomCapacity != null && guestCount.total() <= roomCapacity.value();
    }
}
