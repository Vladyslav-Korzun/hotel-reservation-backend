package com.hotel.management.service.room;

public record RoomOperationResult(
        Long roomId,
        Long hotelId,
        String roomNumber,
        Long roomTypeId,
        int capacity,
        String status
) {
}
