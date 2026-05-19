package com.hotel.management;

import com.hotel.management.service.room.RoomOperationResult;
import com.hotel.management.service.room.RoomOperationsFacade;
import com.hotel.management.service.room.UpdateRoomStatusCommand;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalRoomOperationsFacade implements RoomOperationsFacade {

    private final RoomOperationsFacade delegate;

    public TransactionalRoomOperationsFacade(RoomOperationsFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public RoomOperationResult updateRoomStatus(UpdateRoomStatusCommand command) {
        return delegate.updateRoomStatus(command);
    }
}
