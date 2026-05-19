package com.hotel.management.domain.service.mapper;

import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomOperationResult;

public class RoomOperationResultMapper {

    public RoomOperationResult toResult(Room room) {
        return new RoomOperationResult(
                room.id(),
                room.hotelId(),
                room.number(),
                room.roomTypeId(),
                room.capacity(),
                room.status().name()
        );
    }
}