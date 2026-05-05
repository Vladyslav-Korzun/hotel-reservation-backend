package com.hotel.management.service.staff;

import com.hotel.management.domain.room.Room;

import java.util.Optional;

public interface RoomAssignmentPort {

    Optional<Room> findAvailableRoomForCheckIn(Long hotelId, Long roomTypeId);
}
