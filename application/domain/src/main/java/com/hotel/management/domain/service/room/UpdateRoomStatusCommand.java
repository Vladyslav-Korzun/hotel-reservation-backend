package com.hotel.management.domain.service.room;

import com.hotel.management.domain.room.RoomStatus;

public record UpdateRoomStatusCommand(Long roomId, RoomStatus status) {
}
