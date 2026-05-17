package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;

public class RoomFactory {

    public Room create(Long roomId, Long hotelId, String roomNumber, Long roomTypeId, int capacity, RoomStatus status) {
        if (status == RoomStatus.OCCUPIED) {
            throw new ValidationException("Room can become occupied only through check-in flow");
        }
        return new Room(roomId, hotelId, roomNumber, roomTypeId, capacity, status);
    }
}
