package com.hotel.management;

import com.hotel.management.domain.service.hotel.CreateHotelCommand;
import com.hotel.management.domain.service.hotel.CreateRoomCommand;
import com.hotel.management.domain.service.hotel.CreateRoomTypeCommand;
import com.hotel.management.domain.service.hotel.CreateServiceOfferingCommand;
import com.hotel.management.domain.service.hotel.DeactivateServiceOfferingCommand;
import com.hotel.management.domain.service.hotel.HotelAdministrationFacade;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.service.hotel.UpdateHotelCommand;
import com.hotel.management.domain.service.hotel.UpdateRoomCommand;
import com.hotel.management.domain.service.hotel.UpdateRoomTypeCommand;
import com.hotel.management.domain.service.hotel.UpdateServiceOfferingCommand;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalHotelAdministrationFacade implements HotelAdministrationFacade {

    private final HotelAdministrationFacade delegate;

    public TransactionalHotelAdministrationFacade(HotelAdministrationFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public HotelResult createHotel(AuthenticatedUser actor, CreateHotelCommand command) {
        return delegate.createHotel(actor, command);
    }

    @Override
    @Transactional
    public HotelResult updateHotel(AuthenticatedUser actor, UpdateHotelCommand command) {
        return delegate.updateHotel(actor, command);
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

    @Override
    @Transactional
    public RoomResult createRoom(AuthenticatedUser actor, CreateRoomCommand command) {
        return delegate.createRoom(actor, command);
    }

    @Override
    @Transactional
    public RoomResult updateRoom(AuthenticatedUser actor, UpdateRoomCommand command) {
        return delegate.updateRoom(actor, command);
    }

    @Override
    @Transactional
    public HotelServiceOfferingResult createServiceOffering(AuthenticatedUser actor, CreateServiceOfferingCommand command) {
        return delegate.createServiceOffering(actor, command);
    }

    @Override
    @Transactional
    public HotelServiceOfferingResult updateServiceOffering(AuthenticatedUser actor, UpdateServiceOfferingCommand command) {
        return delegate.updateServiceOffering(actor, command);
    }

    @Override
    @Transactional
    public void deactivateServiceOffering(AuthenticatedUser actor, DeactivateServiceOfferingCommand command) {
        delegate.deactivateServiceOffering(actor, command);
    }
}
