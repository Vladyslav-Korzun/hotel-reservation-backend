package com.hotel.management.service.hotel;

import java.util.List;

public interface HotelQueryFacade {

    List<HotelResult> listHotels(ListHotelsQuery query);

    HotelResult getHotelDetails(Long hotelId);

    List<HotelServiceOfferingResult> listHotelServices(Long hotelId);
}
