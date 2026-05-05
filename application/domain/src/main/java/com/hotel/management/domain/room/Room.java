package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;

public final class Room {

    private final Long id;
    private final Long hotelId;
    private final String number;
    private final Long roomTypeId;
    private final int capacity;
    private final RoomStatus status;

    public Room(Long id, Long hotelId, String number, Long roomTypeId, int capacity, RoomStatus status) {
        this.id = require(id, "roomId is required");
        this.hotelId = require(hotelId, "hotelId is required");
        this.number = requireText(number, "roomNumber is required");
        this.roomTypeId = require(roomTypeId, "roomTypeId is required");
        if (capacity <= 0) {
            throw new ValidationException("room capacity must be greater than zero");
        }
        this.capacity = capacity;
        this.status = require(status, "roomStatus is required");
    }

    public boolean isBookable() {
        return status == RoomStatus.AVAILABLE;
    }

    public Room occupy() {
        if (status != RoomStatus.AVAILABLE) {
            throw new ValidationException("Only available room can be occupied");
        }
        return withStatus(RoomStatus.OCCUPIED);
    }

    public Room markCleaningAfterCheckOut() {
        if (status != RoomStatus.OCCUPIED) {
            throw new ValidationException("Only occupied room can be moved to cleaning after check-out");
        }
        return withStatus(RoomStatus.CLEANING);
    }

    public Room withStatus(RoomStatus nextStatus) {
        return new Room(id, hotelId, number, roomTypeId, capacity, require(nextStatus, "roomStatus is required"));
    }

    public Long id() {
        return id;
    }

    public Long hotelId() {
        return hotelId;
    }

    public String number() {
        return number;
    }

    public Long roomTypeId() {
        return roomTypeId;
    }

    public int capacity() {
        return capacity;
    }

    public RoomStatus status() {
        return status;
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
