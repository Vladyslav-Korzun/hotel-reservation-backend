package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface RoomAdministrationFacade {

    RoomResult createRoom(AuthenticatedUser actor, CreateRoomCommand command);

    RoomResult updateRoom(AuthenticatedUser actor, UpdateRoomCommand command);
}
