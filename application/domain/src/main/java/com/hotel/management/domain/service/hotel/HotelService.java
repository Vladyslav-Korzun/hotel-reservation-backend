package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.HotelFactory;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public class HotelService implements HotelFacade {

    private final HotelRepository hotelRepository;
    private final HotelFactory hotelFactory;
    private final AuditTrail auditTrail;
    private final HotelQueryResultMapper hotelQueryResultMapper;

    public HotelService(
            HotelRepository hotelRepository,
            HotelFactory hotelFactory,
            AuditTrail auditTrail,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        this.hotelRepository = hotelRepository;
        this.hotelFactory = hotelFactory;
        this.auditTrail = auditTrail;
        this.hotelQueryResultMapper = hotelQueryResultMapper;
    }

    @Override
    public HotelResult createHotel(AuthenticatedUser actor, CreateHotelCommand command) {
        if (command == null) {
            throw new ValidationException("create hotel command is required");
        }
        actor.requireAdmin();
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
        if (command == null) {
            throw new ValidationException("update hotel command is required");
        }
        actor.requireAdmin();
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

    private HotelPolicy hotelPolicy(
            boolean childrenAllowed,
            boolean petsAllowed,
            int infantMaxAge,
            int childMaxAge,
            int adultEquivalentAge
    ) {
        return new HotelPolicy(childrenAllowed, petsAllowed, infantMaxAge, childMaxAge, adultEquivalentAge);
    }
}
