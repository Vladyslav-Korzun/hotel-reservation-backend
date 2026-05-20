package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.exception.ValidationException;

public record CreateRoomCommand(
        Long roomId,
        Long hotelId,
        String roomNumber,
        Long roomTypeId,
        int capacity,
        RoomStatus status
) {

    public CreateRoomCommand {
        require(roomId, "roomId is required");
        require(hotelId, "hotelId is required");
        require(roomTypeId, "roomTypeId is required");
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }
}
