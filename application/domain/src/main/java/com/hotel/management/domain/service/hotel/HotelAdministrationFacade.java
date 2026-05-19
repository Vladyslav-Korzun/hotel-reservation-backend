package com.hotel.management.domain.service.hotel;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface HotelAdministrationFacade {

    HotelResult createHotel(AuthenticatedUser actor, CreateHotelCommand command);

    HotelResult updateHotel(AuthenticatedUser actor, UpdateHotelCommand command);

    RoomTypeResult createRoomType(AuthenticatedUser actor, CreateRoomTypeCommand command);

    RoomTypeResult updateRoomType(AuthenticatedUser actor, UpdateRoomTypeCommand command);

    RoomResult createRoom(AuthenticatedUser actor, CreateRoomCommand command);

    RoomResult updateRoom(AuthenticatedUser actor, UpdateRoomCommand command);

    HotelServiceOfferingResult createServiceOffering(AuthenticatedUser actor, CreateServiceOfferingCommand command);

    HotelServiceOfferingResult updateServiceOffering(AuthenticatedUser actor, UpdateServiceOfferingCommand command);

    void deactivateServiceOffering(AuthenticatedUser actor, DeactivateServiceOfferingCommand command);
}
