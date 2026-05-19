package com.hotel.management.service.room;
import com.hotel.management.domain.room.RoomOperationResult;

public interface RoomOperationsFacade {

    RoomOperationResult updateRoomStatus(UpdateRoomStatusCommand command);
}