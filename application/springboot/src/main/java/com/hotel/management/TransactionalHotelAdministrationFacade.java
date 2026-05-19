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
import org.springframework.transaction.annotation.Transactional;

public class TransactionalHotelAdministrationFacade implements HotelAdministrationFacade {

    private final HotelAdministrationFacade delegate;

    public TransactionalHotelAdministrationFacade(HotelAdministrationFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public HotelResult createHotel(CreateHotelCommand command) {
        return delegate.createHotel(command);
    }

    @Override
    @Transactional
    public HotelResult updateHotel(UpdateHotelCommand command) {
        return delegate.updateHotel(command);
    }

    @Override
    @Transactional
    public RoomTypeResult createRoomType(CreateRoomTypeCommand command) {
        return delegate.createRoomType(command);
    }

    @Override
    @Transactional
    public RoomTypeResult updateRoomType(UpdateRoomTypeCommand command) {
        return delegate.updateRoomType(command);
    }

    @Override
    @Transactional
    public RoomResult createRoom(CreateRoomCommand command) {
        return delegate.createRoom(command);
    }

    @Override
    @Transactional
    public RoomResult updateRoom(UpdateRoomCommand command) {
        return delegate.updateRoom(command);
    }

    @Override
    @Transactional
    public HotelServiceOfferingResult createServiceOffering(CreateServiceOfferingCommand command) {
        return delegate.createServiceOffering(command);
    }

    @Override
    @Transactional
    public HotelServiceOfferingResult updateServiceOffering(UpdateServiceOfferingCommand command) {
        return delegate.updateServiceOffering(command);
    }

    @Override
    @Transactional
    public void deactivateServiceOffering(DeactivateServiceOfferingCommand command) {
        delegate.deactivateServiceOffering(command);
    }
}