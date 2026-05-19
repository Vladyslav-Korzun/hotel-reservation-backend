package com.hotel.management.domain.service.room;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.service.exception.ForbiddenException;
import com.hotel.management.domain.audit.AuditLogPort;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.predicate.reservation.IsStaffOrAdminPredicate;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.domain.service.mapper.RoomOperationResultMapper;

public class RoomOperationsService implements RoomOperationsFacade {

    private final RoomRepository roomRepository;
    private final CurrentUserPort currentUserPort;
    private final ClockPort clockPort;
    private final AuditLogPort auditLogPort;
    private final RoomOperationResultMapper roomOperationResultMapper;

    public RoomOperationsService(
            RoomRepository roomRepository,
            CurrentUserPort currentUserPort,
            ClockPort clockPort,
            AuditLogPort auditLogPort,
            RoomOperationResultMapper roomOperationResultMapper
    ) {
        this.roomRepository = roomRepository;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
        this.auditLogPort = auditLogPort;
        this.roomOperationResultMapper = roomOperationResultMapper;
    }

    @Override
    public RoomOperationResult updateRoomStatus(UpdateRoomStatusCommand command) {
        var currentUser = requireStaffOrAdmin();
        requireCommand(command);

        var room = roomRepository.findById(command.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found: " + command.roomId()));
        var updatedRoom = changeStatus(room, command.status());

        var savedRoom = roomRepository.save(updatedRoom);
        auditLogPort.append(auditEntry(currentUser, savedRoom));
        return roomOperationResultMapper.toResult(savedRoom);
    }

    private AuthenticatedUser requireStaffOrAdmin() {
        var currentUser = currentUserPort.getCurrentUser();
        if (IsStaffOrAdminPredicate.INSTANCE.test(currentUser)) {
            return currentUser;
        }
        throw new ForbiddenException("Only staff or admin can manage room operations");
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

    private AuditLogEntry auditEntry(AuthenticatedUser currentUser, Room room) {
        return new AuditLogEntry(
                null,
                currentUser.userId(),
                currentUser.roles().stream().findFirst().orElse("UNKNOWN"),
                AuditActionType.UPDATE_ROOM_STATUS,
                AuditEntityType.ROOM,
                String.valueOf(room.id()),
                clockPort.now(),
                "Room status changed to " + room.status().name()
        );
    }
}
