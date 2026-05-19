package com.hotel.management.service.hotel;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomTypeResult;

public interface HotelAdministrationFacade {

    HotelResult createHotel(CreateHotelCommand command);

    HotelResult updateHotel(UpdateHotelCommand command);

    RoomTypeResult createRoomType(CreateRoomTypeCommand command);

    RoomTypeResult updateRoomType(UpdateRoomTypeCommand command);

    RoomResult createRoom(CreateRoomCommand command);

    RoomResult updateRoom(UpdateRoomCommand command);

    HotelServiceOfferingResult createServiceOffering(CreateServiceOfferingCommand command);

    HotelServiceOfferingResult updateServiceOffering(UpdateServiceOfferingCommand command);

    void deactivateServiceOffering(DeactivateServiceOfferingCommand command);
}