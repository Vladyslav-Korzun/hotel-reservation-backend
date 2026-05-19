package com.hotel.management.domain.service.room;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.domain.service.mapper.RoomOperationResultMapper;

public class RoomOperationsService implements RoomOperationsFacade {

    private final RoomRepository roomRepository;
    private final AuditTrail auditTrail;
    private final RoomOperationResultMapper roomOperationResultMapper;

    public RoomOperationsService(
            RoomRepository roomRepository,
            AuditTrail auditTrail,
            RoomOperationResultMapper roomOperationResultMapper
    ) {
        this.roomRepository = roomRepository;
        this.auditTrail = auditTrail;
        this.roomOperationResultMapper = roomOperationResultMapper;
    }

    @Override
    public RoomOperationResult updateRoomStatus(AuthenticatedUser actor, UpdateRoomStatusCommand command) {
        actor.requireStaffOrAdmin();
        requireCommand(command);

        var room = roomRepository.findById(command.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found: " + command.roomId()));
        var updatedRoom = changeStatus(room, command.status());

        var savedRoom = roomRepository.save(updatedRoom);
        auditTrail.record(
                actor,
                AuditActionType.UPDATE_ROOM_STATUS,
                AuditEntityType.ROOM,
                String.valueOf(savedRoom.id()),
                "Room status changed to " + savedRoom.status().name()
        );
        return roomOperationResultMapper.toResult(savedRoom);
    }

    private void requireCommand(UpdateRoomStatusCommand command) {
        if (command == null) {
            throw new ValidationException("update room status command is required");
        }
        if (command.roomId() == null) {
            throw new ValidationException("roomId is required");
        }
        if (command.status() == null) {
            throw new ValidationException("roomStatus is required");
        }
    }

    private Room changeStatus(Room room, RoomStatus status) {
        return switch (status) {
            case AVAILABLE -> room.markAvailable();
            case CLEANING -> room.markCleaning();
            case MAINTENANCE -> room.markMaintenance();
            case OUT_OF_SERVICE -> room.markOutOfService();
            case OCCUPIED -> throw new ValidationException("Room can become occupied only through check-in flow");
        };
    }

}
