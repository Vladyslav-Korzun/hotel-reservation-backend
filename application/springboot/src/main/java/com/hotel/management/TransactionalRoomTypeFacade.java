package com.hotel.management;

import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.service.hotel.CreateRoomTypeCommand;
import com.hotel.management.domain.service.hotel.RoomTypeFacade;
import com.hotel.management.domain.service.hotel.UpdateRoomTypeCommand;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalRoomTypeFacade implements RoomTypeFacade {

    private final RoomTypeFacade delegate;

    public TransactionalRoomTypeFacade(RoomTypeFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public RoomTypeResult createRoomType(AuthenticatedUser actor, CreateRoomTypeCommand command) {
        return delegate.createRoomType(actor, command);
    }

    @Override
    @Transactional
    public RoomTypeResult updateRoomType(AuthenticatedUser actor, UpdateRoomTypeCommand command) {
        return delegate.updateRoomType(actor, command);
    }
}
