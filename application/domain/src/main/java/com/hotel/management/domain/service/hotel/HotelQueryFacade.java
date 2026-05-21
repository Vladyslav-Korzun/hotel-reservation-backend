package com.hotel.management.domain.service.hotel;

import java.util.List;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.room.RoomTypeResult;

public interface HotelQueryFacade {

    List<HotelResult> listHotels(ListHotelsQuery query);

    HotelResult getHotelDetails(Long hotelId);

    List<HotelServiceOfferingResult> listHotelServices(Long hotelId);

    List<RoomTypeResult> listRoomTypes(Long hotelId);
}