package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.RoomTypeFactory;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public class RoomTypeService implements RoomTypeFacade {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeFactory roomTypeFactory;
    private final AuditTrail auditTrail;
    private final HotelQueryResultMapper hotelQueryResultMapper;

    public RoomTypeService(
            RoomTypeRepository roomTypeRepository,
            HotelRepository hotelRepository,
            RoomTypeFactory roomTypeFactory,
            AuditTrail auditTrail,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeFactory = roomTypeFactory;
        this.auditTrail = auditTrail;
        this.hotelQueryResultMapper = hotelQueryResultMapper;
    }

    @Override
    public RoomTypeResult createRoomType(AuthenticatedUser actor, CreateRoomTypeCommand command) {
        if (command == null) {
            throw new ValidationException("create room type command is required");
        }
        actor.requireAdmin();
        assertHotelExists(command.hotelId());
        if (roomTypeRepository.findById(command.roomTypeId()).isPresent()) {
            throw new ValidationException("Room type already exists: " + command.roomTypeId());
        }

        var roomType = roomTypeFactory.create(
                command.roomTypeId(),
                command.hotelId(),
                command.name(),
                RoomTypeCommandAssembler.toOccupancyPolicy(command),
                RoomTypeCommandAssembler.toPetPolicy(command),
                RoomTypeCommandAssembler.toBasePrice(command),
                command.description(),
                RoomTypeCommandAssembler.toFeatures(command)
        );
        var savedRoomType = roomTypeRepository.save(roomType);
        auditTrail.record(
                actor,
                AuditActionType.CREATE_ROOM_TYPE,
                AuditEntityType.ROOM_TYPE,
                String.valueOf(savedRoomType.id()),
                "Room type created"
        );
        return hotelQueryResultMapper.toResult(savedRoomType);
    }

    @Override
    public RoomTypeResult updateRoomType(AuthenticatedUser actor, UpdateRoomTypeCommand command) {
        if (command == null) {
            throw new ValidationException("update room type command is required");
        }
        actor.requireAdmin();
        var roomType = roomTypeRepository.findById(command.roomTypeId())
                .orElseThrow(() -> new NotFoundException("Room type not found: " + command.roomTypeId()));
        assertHotelExists(roomType.hotelId());

        var updatedRoomType = roomType.updateDetails(
                command.name(),
                RoomTypeCommandAssembler.toOccupancyPolicy(command),
                RoomTypeCommandAssembler.toPetPolicy(command),
                RoomTypeCommandAssembler.toBasePrice(command),
                command.description(),
                RoomTypeCommandAssembler.toFeatures(command)
        );
        var savedRoomType = roomTypeRepository.save(updatedRoomType);
        auditTrail.record(
                actor,
                AuditActionType.UPDATE_ROOM_TYPE,
                AuditEntityType.ROOM_TYPE,
                String.valueOf(savedRoomType.id()),
                "Room type updated"
        );
        return hotelQueryResultMapper.toResult(savedRoomType);
    }

    private void assertHotelExists(Long hotelId) {
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + hotelId));
    }

}
