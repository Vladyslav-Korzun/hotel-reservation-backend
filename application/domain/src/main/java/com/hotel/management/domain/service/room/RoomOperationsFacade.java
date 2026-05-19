package com.hotel.management.domain.service.room;
import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface RoomOperationsFacade {

    RoomOperationResult updateRoomStatus(AuthenticatedUser actor, UpdateRoomStatusCommand command);
}
