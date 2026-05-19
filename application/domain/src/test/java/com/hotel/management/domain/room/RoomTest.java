package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomTest {

    @Test
    void shouldMarkCleaningRoomAvailable() {
        var room = room(RoomStatus.CLEANING);

        var result = room.markAvailable();

        assertEquals(RoomStatus.AVAILABLE, result.status());
    }

    @Test
    void shouldRejectOccupiedRoomMarkedAvailable() {
        var room = room(RoomStatus.OCCUPIED);

        assertThrows(ValidationException.class, room::markAvailable);
    }

    @Test
    void shouldRejectOccupiedRoomMarkedMaintenance() {
        var room = room(RoomStatus.OCCUPIED);

        assertThrows(ValidationException.class, room::markMaintenance);
    }

    @Test
    void shouldRejectOutOfServiceRoomMarkedAvailableDirectly() {
        var room = room(RoomStatus.OUT_OF_SERVICE);

        assertThrows(ValidationException.class, room::markAvailable);
    }

    private static Room room(RoomStatus status) {
        return new Room(1L, 10L, "101", 20L, 2, status);
    }
}
