package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface RoomTypeFacade {

    RoomTypeResult createRoomType(AuthenticatedUser actor, CreateRoomTypeCommand command);

    RoomTypeResult updateRoomType(AuthenticatedUser actor, UpdateRoomTypeCommand command);
}
