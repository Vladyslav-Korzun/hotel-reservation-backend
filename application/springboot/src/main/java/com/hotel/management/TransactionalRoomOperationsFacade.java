package com.hotel.management;

import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.domain.service.room.RoomOperationsFacade;
import com.hotel.management.domain.service.room.UpdateRoomStatusCommand;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalRoomOperationsFacade implements RoomOperationsFacade {

    private final RoomOperationsFacade delegate;

    public TransactionalRoomOperationsFacade(RoomOperationsFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public RoomOperationResult updateRoomStatus(AuthenticatedUser actor, UpdateRoomStatusCommand command) {
        return delegate.updateRoomStatus(actor, command);
    }
}
