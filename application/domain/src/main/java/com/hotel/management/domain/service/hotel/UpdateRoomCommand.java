package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.exception.ValidationException;

public record UpdateRoomCommand(
        Long roomId,
        Long hotelId,
        String roomNumber,
        Long roomTypeId,
        int capacity,
        RoomStatus status
) {

    public UpdateRoomCommand {
        require(roomId, "roomId is required");
        require(hotelId, "hotelId is required");
        requireText(roomNumber, "roomNumber is required");
        require(roomTypeId, "roomTypeId is required");
        if (capacity <= 0) {
            throw new ValidationException("room capacity must be greater than zero");
        }
        require(status, "roomStatus is required");
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value;
    }
}
