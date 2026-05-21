package com.hotel.management.domain.service.room;
import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

import java.util.List;

public interface RoomOperationsFacade {

    RoomOperationResult updateRoomStatus(AuthenticatedUser actor, UpdateRoomStatusCommand command);

    List<RoomOperationResult> listRooms(AuthenticatedUser actor, Long hotelId);
}
