package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.HotelFactory;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomFactory;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeFactory;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingFactory;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;

public class HotelAdministrationService implements HotelAdministrationFacade {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final AuditTrail auditTrail;
    private final HotelFactory hotelFactory;
    private final RoomTypeFactory roomTypeFactory;
    private final RoomFactory roomFactory;
    private final ServiceOfferingFactory serviceOfferingFactory;
    private final HotelQueryResultMapper hotelQueryResultMapper;

    public HotelAdministrationService(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            RoomRepository roomRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            AuditTrail auditTrail,
            HotelFactory hotelFactory,
            RoomTypeFactory roomTypeFactory,
            RoomFactory roomFactory,
            ServiceOfferingFactory serviceOfferingFactory,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.auditTrail = auditTrail;
        this.hotelFactory = hotelFactory;
        this.roomTypeFactory = roomTypeFactory;
        this.roomFactory = roomFactory;
        this.serviceOfferingFactory = serviceOfferingFactory;
        this.hotelQueryResultMapper = hotelQueryResultMapper;
    }

    @Override
    public HotelResult createHotel(AuthenticatedUser actor, CreateHotelCommand command) {
        actor.requireAdmin();
        requireCreateCommand(command);
        if (hotelRepository.findById(command.hotelId()).isPresent()) {
            throw new ValidationException("Hotel already exists: " + command.hotelId());
        }

        var hotel = hotelFactory.create(
                command.hotelId(),
                command.name(),
                command.city(),
                command.country(),
                command.address(),
                command.stars(),
                command.description(),
                command.status(),
                hotelPolicy(
                        command.childrenAllowed(),
                        command.petsAllowed(),
                        command.infantMaxAge(),
                        command.childMaxAge(),
                        command.adultEquivalentAge()
                )
        );
        var savedHotel = hotelRepository.save(hotel);
        auditTrail.record(actor, AuditActionType.CREATE_HOTEL, AuditEntityType.HOTEL, String.valueOf(savedHotel.id()), "Hotel created");
        return hotelQueryResultMapper.toResult(savedHotel);
    }

    @Override
    public HotelResult updateHotel(AuthenticatedUser actor, UpdateHotelCommand command) {
        actor.requireAdmin();
        requireUpdateCommand(command);
        var hotel = hotelRepository.findById(command.hotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + command.hotelId()));

        var updatedHotel = hotel.updateDetails(
                command.name(),
                command.city(),
                command.country(),
                command.address(),
                command.stars(),
                command.description(),
                command.status(),
                hotelPolicy(
                        command.childrenAllowed(),
                        command.petsAllowed(),
                        command.infantMaxAge(),
                        command.childMaxAge(),
                        command.adultEquivalentAge()
                )
        );
        var savedHotel = hotelRepository.save(updatedHotel);
        auditTrail.record(actor, AuditActionType.UPDATE_HOTEL, AuditEntityType.HOTEL, String.valueOf(savedHotel.id()), "Hotel updated");
        return hotelQueryResultMapper.toResult(savedHotel);
    }

    @Override
    public RoomTypeResult createRoomType(AuthenticatedUser actor, CreateRoomTypeCommand command) {
        actor.requireAdmin();
        requireCreateRoomTypeCommand(command);
        assertHotelExists(command.hotelId());
        if (roomTypeRepository.findById(command.roomTypeId()).isPresent()) {
            throw new ValidationException("Room type already exists: " + command.roomTypeId());
        }

        var roomType = roomTypeFactory.create(
                command.roomTypeId(),
                command.hotelId(),
                command.name(),
                occupancyPolicy(command),
                petPolicy(command),
                money(command.basePriceAmount(), command.basePriceCurrency(), "basePrice"),
                command.description(),
                roomTypeFeatures(command)
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
        actor.requireAdmin();
        requireUpdateRoomTypeCommand(command);
        var roomType = roomTypeRepository.findById(command.roomTypeId())
                .orElseThrow(() -> new NotFoundException("Room type not found: " + command.roomTypeId()));
        assertHotelExists(roomType.hotelId());

        var updatedRoomType = roomType.updateDetails(
                command.name(),
                occupancyPolicy(command),
                petPolicy(command),
                money(command.basePriceAmount(), command.basePriceCurrency(), "basePrice"),
                command.description(),
                roomTypeFeatures(command)
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

    @Override
    public RoomResult createRoom(AuthenticatedUser actor, CreateRoomCommand command) {
        actor.requireAdmin();
        requireCreateRoomCommand(command);
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
        actor.requireAdmin();
        requireUpdateRoomCommand(command);
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

    @Override
    public HotelServiceOfferingResult createServiceOffering(AuthenticatedUser actor, CreateServiceOfferingCommand command) {
        actor.requireAdmin();
        requireCreateServiceOfferingCommand(command);
        assertHotelExists(command.hotelId());
        if (serviceOfferingRepository.findById(command.serviceOfferingId()).isPresent()) {
            throw new ValidationException("Service offering already exists: " + command.serviceOfferingId());
        }

        var serviceOffering = serviceOfferingFactory.create(
                command.serviceOfferingId(),
                command.hotelId(),
                command.code(),
                command.name(),
                command.description(),
                money(command.priceAmount(), command.priceCurrency(), "serviceOfferingPrice"),
                command.active(),
                command.availabilityRule()
        );
        var savedServiceOffering = serviceOfferingRepository.save(serviceOffering);
        auditTrail.record(
                actor,
                AuditActionType.CREATE_SERVICE_OFFERING,
                AuditEntityType.SERVICE_OFFERING,
                String.valueOf(savedServiceOffering.id()),
                "Service offering created"
        );
        return hotelQueryResultMapper.toResult(savedServiceOffering);
    }

    @Override
    public HotelServiceOfferingResult updateServiceOffering(AuthenticatedUser actor, UpdateServiceOfferingCommand command) {
        actor.requireAdmin();
        requireUpdateServiceOfferingCommand(command);
        assertHotelExists(command.hotelId());
        var serviceOffering = loadServiceOffering(command.serviceOfferingId());
        assertServiceOfferingBelongsToHotel(serviceOffering, command.hotelId());

        var updatedServiceOffering = serviceOffering.updateDetails(
                command.code(),
                command.name(),
                command.description(),
                money(command.priceAmount(), command.priceCurrency(), "serviceOfferingPrice"),
                command.active(),
                command.availabilityRule()
        );
        var savedServiceOffering = serviceOfferingRepository.save(updatedServiceOffering);
        auditTrail.record(
                actor,
                AuditActionType.UPDATE_SERVICE_OFFERING,
                AuditEntityType.SERVICE_OFFERING,
                String.valueOf(savedServiceOffering.id()),
                "Service offering updated"
        );
        return hotelQueryResultMapper.toResult(savedServiceOffering);
    }

    @Override
    public void deactivateServiceOffering(AuthenticatedUser actor, DeactivateServiceOfferingCommand command) {
        actor.requireAdmin();
        requireDeactivateServiceOfferingCommand(command);
        assertHotelExists(command.hotelId());
        var serviceOffering = loadServiceOffering(command.serviceOfferingId());
        assertServiceOfferingBelongsToHotel(serviceOffering, command.hotelId());

        var deactivatedServiceOffering = serviceOfferingRepository.save(serviceOffering.deactivate());
        auditTrail.record(
                actor,
                AuditActionType.DEACTIVATE_SERVICE_OFFERING,
                AuditEntityType.SERVICE_OFFERING,
                String.valueOf(deactivatedServiceOffering.id()),
                "Service offering deactivated"
        );
    }

    private void requireCreateCommand(CreateHotelCommand command) {
        if (command == null) {
            throw new ValidationException("create hotel command is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
    }

    private void requireUpdateCommand(UpdateHotelCommand command) {
        if (command == null) {
            throw new ValidationException("update hotel command is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
    }

    private void requireCreateRoomTypeCommand(CreateRoomTypeCommand command) {
        if (command == null) {
            throw new ValidationException("create room type command is required");
        }
        if (command.roomTypeId() == null) {
            throw new ValidationException("roomTypeId is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
    }

    private void requireUpdateRoomTypeCommand(UpdateRoomTypeCommand command) {
        if (command == null) {
            throw new ValidationException("update room type command is required");
        }
        if (command.roomTypeId() == null) {
            throw new ValidationException("roomTypeId is required");
        }
    }

    private void requireCreateRoomCommand(CreateRoomCommand command) {
        if (command == null) {
            throw new ValidationException("create room command is required");
        }
        if (command.roomId() == null) {
            throw new ValidationException("roomId is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
        if (command.roomTypeId() == null) {
            throw new ValidationException("roomTypeId is required");
        }
        if (command.status() == null) {
            throw new ValidationException("roomStatus is required");
        }
    }

    private void requireUpdateRoomCommand(UpdateRoomCommand command) {
        if (command == null) {
            throw new ValidationException("update room command is required");
        }
        if (command.roomId() == null) {
            throw new ValidationException("roomId is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
        if (command.roomTypeId() == null) {
            throw new ValidationException("roomTypeId is required");
        }
        if (command.status() == null) {
            throw new ValidationException("roomStatus is required");
        }
    }

    private void requireCreateServiceOfferingCommand(CreateServiceOfferingCommand command) {
        if (command == null) {
            throw new ValidationException("create service offering command is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
        if (command.serviceOfferingId() == null) {
            throw new ValidationException("serviceOfferingId is required");
        }
    }

    private void requireUpdateServiceOfferingCommand(UpdateServiceOfferingCommand command) {
        if (command == null) {
            throw new ValidationException("update service offering command is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
        if (command.serviceOfferingId() == null) {
            throw new ValidationException("serviceOfferingId is required");
        }
    }

    private void requireDeactivateServiceOfferingCommand(DeactivateServiceOfferingCommand command) {
        if (command == null) {
            throw new ValidationException("deactivate service offering command is required");
        }
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
        if (command.serviceOfferingId() == null) {
            throw new ValidationException("serviceOfferingId is required");
        }
    }

    private HotelPolicy hotelPolicy(
            boolean childrenAllowed,
            boolean petsAllowed,
            int infantMaxAge,
            int childMaxAge,
            int adultEquivalentAge
    ) {
        return new HotelPolicy(childrenAllowed, petsAllowed, infantMaxAge, childMaxAge, adultEquivalentAge);
    }

    private void assertHotelExists(Long hotelId) {
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + hotelId));
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

    private void assertAdminRoomStatusAllowed(RoomStatus status) {
        if (status == RoomStatus.OCCUPIED) {
            throw new ValidationException("Room can become occupied only through check-in flow");
        }
    }

    private ServiceOffering loadServiceOffering(Long serviceOfferingId) {
        return serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new NotFoundException("Service offering not found: " + serviceOfferingId));
    }

    private void assertServiceOfferingBelongsToHotel(ServiceOffering serviceOffering, Long hotelId) {
        if (!serviceOffering.belongsToHotel(hotelId)) {
            throw new ValidationException("Service offering does not belong to hotel: " + hotelId);
        }
    }

    private OccupancyPolicy occupancyPolicy(CreateRoomTypeCommand command) {
        return new OccupancyPolicy(command.maxAdults(), command.maxChildren(), command.maxInfants(), command.maxTotalGuests());
    }

    private OccupancyPolicy occupancyPolicy(UpdateRoomTypeCommand command) {
        return new OccupancyPolicy(command.maxAdults(), command.maxChildren(), command.maxInfants(), command.maxTotalGuests());
    }

    private PetPolicy petPolicy(CreateRoomTypeCommand command) {
        return new PetPolicy(
                command.petsAllowed(),
                command.maxPets(),
                command.allowedPetTypes(),
                command.maxPetWeightKg(),
                optionalMoney(command.petFeeAmount(), command.petFeeCurrency(), "petFee")
        );
    }

    private PetPolicy petPolicy(UpdateRoomTypeCommand command) {
        return new PetPolicy(
                command.petsAllowed(),
                command.maxPets(),
                command.allowedPetTypes(),
                command.maxPetWeightKg(),
                optionalMoney(command.petFeeAmount(), command.petFeeCurrency(), "petFee")
        );
    }

    private RoomTypeFeatures roomTypeFeatures(CreateRoomTypeCommand command) {
        return new RoomTypeFeatures(command.bedSetup(), command.roomSizeSqm(), command.amenities());
    }

    private RoomTypeFeatures roomTypeFeatures(UpdateRoomTypeCommand command) {
        return new RoomTypeFeatures(command.bedSetup(), command.roomSizeSqm(), command.amenities());
    }

    private Money money(java.math.BigDecimal amount, String currencyCode, String fieldName) {
        if (amount == null) {
            throw new ValidationException(fieldName + " amount is required");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new ValidationException(fieldName + " currency is required");
        }
        return new Money(amount, java.util.Currency.getInstance(currencyCode));
    }

    private Money optionalMoney(java.math.BigDecimal amount, String currencyCode, String fieldName) {
        if (amount == null && (currencyCode == null || currencyCode.isBlank())) {
            return null;
        }
        return money(amount, currencyCode, fieldName);
    }

}
