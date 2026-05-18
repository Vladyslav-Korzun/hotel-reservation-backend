package com.hotel.management.service.hotel;

import com.hotel.management.domain.room.RoomStatus;

public record CreateRoomCommand(
        Long roomId,
        Long hotelId,
        String roomNumber,
        Long roomTypeId,
        int capacity,
        RoomStatus status
) {
}
