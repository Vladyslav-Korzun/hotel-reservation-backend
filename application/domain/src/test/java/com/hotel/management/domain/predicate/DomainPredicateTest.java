package com.hotel.management.domain.predicate;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.predicate.hotel.IsActiveHotelPredicate;
import com.hotel.management.domain.predicate.room.IsBookableRoomPredicate;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainPredicateTest {

    @Test
    void shouldDetectActiveHotel() {
        assertTrue(IsActiveHotelPredicate.INSTANCE.test(hotel(HotelStatus.ACTIVE)));
        assertFalse(IsActiveHotelPredicate.INSTANCE.test(hotel(HotelStatus.INACTIVE)));
        assertFalse(IsActiveHotelPredicate.INSTANCE.test(null));
    }

    @Test
    void shouldDetectBookableRoom() {
        assertTrue(IsBookableRoomPredicate.INSTANCE.test(room(RoomStatus.AVAILABLE)));
        assertFalse(IsBookableRoomPredicate.INSTANCE.test(room(RoomStatus.OCCUPIED)));
        assertFalse(IsBookableRoomPredicate.INSTANCE.test(null));
    }

    private static Hotel hotel(HotelStatus status) {
        return new Hotel(
                1L,
                "Danube Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                status,
                new HotelPolicy(true, true, 2, 12, 13)
        );
    }

    private static Room room(RoomStatus status) {
        return new Room(100L, 1L, "101", 2L, 2, status);
    }
}
