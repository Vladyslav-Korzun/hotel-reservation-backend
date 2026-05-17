package com.hotel.management.domain.predicate.room;

import com.hotel.management.domain.room.Room;

import java.util.function.Predicate;

public final class IsBookableRoomPredicate implements Predicate<Room> {

    public static final IsBookableRoomPredicate INSTANCE = new IsBookableRoomPredicate();

    private IsBookableRoomPredicate() {
    }

    @Override
    public boolean test(Room room) {
        return room != null && room.isBookable();
    }
}
