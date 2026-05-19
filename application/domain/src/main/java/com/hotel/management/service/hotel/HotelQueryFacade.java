package com.hotel.management.service.hotel;

import java.util.List;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;

public interface HotelQueryFacade {

    List<HotelResult> listHotels(ListHotelsQuery query);

    HotelResult getHotelDetails(Long hotelId);

    List<HotelServiceOfferingResult> listHotelServices(Long hotelId);
}