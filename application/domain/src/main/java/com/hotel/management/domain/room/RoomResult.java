package com.hotel.management.domain.room;

public record RoomResult(
        Long roomId,
        Long hotelId,
        String roomNumber,
        Long roomTypeId,
        int capacity,
        String status
) {
}
