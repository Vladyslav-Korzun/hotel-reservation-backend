package com.hotel.management;

import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.service.hotel.CreateRoomCommand;
import com.hotel.management.domain.service.hotel.RoomAdministrationFacade;
import com.hotel.management.domain.service.hotel.UpdateRoomCommand;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalRoomAdministrationFacade implements RoomAdministrationFacade {

    private final RoomAdministrationFacade delegate;

    public TransactionalRoomAdministrationFacade(RoomAdministrationFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public RoomResult createRoom(AuthenticatedUser actor, CreateRoomCommand command) {
        return delegate.createRoom(actor, command);
    }

    @Override
    @Transactional
    public RoomResult updateRoom(AuthenticatedUser actor, UpdateRoomCommand command) {
        return delegate.updateRoom(actor, command);
    }
}
