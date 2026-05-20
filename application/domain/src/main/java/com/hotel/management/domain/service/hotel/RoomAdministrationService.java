package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.RoomFactory;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public class RoomAdministrationService implements RoomAdministrationFacade {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final RoomFactory roomFactory;
    private final AuditTrail auditTrail;
    private final HotelQueryResultMapper hotelQueryResultMapper;

    public RoomAdministrationService(
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            HotelRepository hotelRepository,
            RoomFactory roomFactory,
            AuditTrail auditTrail,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.roomFactory = roomFactory;
        this.auditTrail = auditTrail;
        this.hotelQueryResultMapper = hotelQueryResultMapper;
    }

    @Override
    public RoomResult createRoom(AuthenticatedUser actor, CreateRoomCommand command) {
        if (command == null) {
            throw new ValidationException("create room command is required");
        }
        actor.requireAdmin();
        var roomType = loadRoomType(command.roomTypeId());
        assertRoomTypeBelongsToHotel(roomType, command.hotelId());
        if (roomRepository.findById(command.roomId()).isPresent()) {
            throw new ValidationException("Room already exists: " + command.roomId());
        }
        assertAdminRoomStatusAllowed(command.status());

        var room = roomFactory.create(
                command.roomId(),
                command.hotelId(),
                command.roomNumber(),
                command.roomTypeId(),
                command.capacity(),
                command.status()
        );
        var savedRoom = roomRepository.save(room);
        auditTrail.record(
                actor,
                AuditActionType.CREATE_ROOM,
                AuditEntityType.ROOM,
                String.valueOf(savedRoom.id()),
                "Room created"
        );
        return hotelQueryResultMapper.toResult(savedRoom);
    }

    @Override
    public RoomResult updateRoom(AuthenticatedUser actor, UpdateRoomCommand command) {
        if (command == null) {
            throw new ValidationException("update room command is required");
        }
        actor.requireAdmin();
        var room = roomRepository.findById(command.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found: " + command.roomId()));
        var roomType = loadRoomType(command.roomTypeId());
        assertRoomTypeBelongsToHotel(roomType, command.hotelId());
        assertAdminRoomStatusAllowed(command.status());

        var updatedRoom = room.updateDetails(
                command.hotelId(),
                command.roomNumber(),
                command.roomTypeId(),
                command.capacity(),
                command.status()
        );
        var savedRoom = roomRepository.save(updatedRoom);
        auditTrail.record(
                actor,
                AuditActionType.UPDATE_ROOM,
                AuditEntityType.ROOM,
                String.valueOf(savedRoom.id()),
                "Room updated"
        );
        return hotelQueryResultMapper.toResult(savedRoom);
    }

    private RoomType loadRoomType(Long roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("Room type not found: " + roomTypeId));
    }

    private void assertRoomTypeBelongsToHotel(RoomType roomType, Long hotelId) {
        assertHotelExists(hotelId);
        if (!roomType.hotelId().equals(hotelId)) {
            throw new ValidationException("Room type does not belong to hotel: " + hotelId);
        }
    }

    private void assertHotelExists(Long hotelId) {
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + hotelId));
    }

    private void assertAdminRoomStatusAllowed(RoomStatus status) {
        if (status == RoomStatus.OCCUPIED) {
            throw new ValidationException("Room can become occupied only through check-in flow");
        }
    }
}
