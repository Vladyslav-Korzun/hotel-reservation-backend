package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingFactory;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.Money;

public class ServiceOfferingService implements ServiceOfferingFacade {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final HotelRepository hotelRepository;
    private final ServiceOfferingFactory serviceOfferingFactory;
    private final AuditTrail auditTrail;
    private final HotelQueryResultMapper hotelQueryResultMapper;

    public ServiceOfferingService(
            ServiceOfferingRepository serviceOfferingRepository,
            HotelRepository hotelRepository,
            ServiceOfferingFactory serviceOfferingFactory,
            AuditTrail auditTrail,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.hotelRepository = hotelRepository;
        this.serviceOfferingFactory = serviceOfferingFactory;
        this.auditTrail = auditTrail;
        this.hotelQueryResultMapper = hotelQueryResultMapper;
    }

    @Override
    public HotelServiceOfferingResult createServiceOffering(AuthenticatedUser actor, CreateServiceOfferingCommand command) {
        if (command == null) {
            throw new ValidationException("create service offering command is required");
        }
        actor.requireAdmin();
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
                Money.of(command.priceAmount(), command.priceCurrency()),
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
        if (command == null) {
            throw new ValidationException("update service offering command is required");
        }
        actor.requireAdmin();
        assertHotelExists(command.hotelId());
        var serviceOffering = loadServiceOffering(command.serviceOfferingId());
        assertServiceOfferingBelongsToHotel(serviceOffering, command.hotelId());

        var updatedServiceOffering = serviceOffering.updateDetails(
                command.code(),
                command.name(),
                command.description(),
                Money.of(command.priceAmount(), command.priceCurrency()),
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
        if (command == null) {
            throw new ValidationException("deactivate service offering command is required");
        }
        actor.requireAdmin();
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

    private void assertHotelExists(Long hotelId) {
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + hotelId));
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

}
