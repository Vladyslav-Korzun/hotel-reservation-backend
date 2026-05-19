package com.hotel.management.controller;

import com.hotel.management.api.HotelsApi;
import com.hotel.management.api.dto.HotelResponse;
import com.hotel.management.api.dto.HotelServiceOfferingResponse;
import com.hotel.management.api.dto.RoomTypeAvailabilityCalendarDayResponse;
import com.hotel.management.mapper.AvailabilityMapper;
import com.hotel.management.mapper.HotelMapper;
import com.hotel.management.service.availability.GetRoomTypeAvailabilityCalendarFacade;
import com.hotel.management.service.hotel.HotelQueryFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class HotelsController implements HotelsApi {

    private final HotelQueryFacade hotelQueryFacade;
    private final GetRoomTypeAvailabilityCalendarFacade getRoomTypeAvailabilityCalendarFacade;
    private final HotelMapper hotelMapper;
    private final AvailabilityMapper availabilityMapper;

    public HotelsController(
            HotelQueryFacade hotelQueryFacade,
            GetRoomTypeAvailabilityCalendarFacade getRoomTypeAvailabilityCalendarFacade,
            HotelMapper hotelMapper,
            AvailabilityMapper availabilityMapper
    ) {
        this.hotelQueryFacade = hotelQueryFacade;
        this.getRoomTypeAvailabilityCalendarFacade = getRoomTypeAvailabilityCalendarFacade;
        this.hotelMapper = hotelMapper;
        this.availabilityMapper = availabilityMapper;
    }

    @Override
    public ResponseEntity<List<HotelResponse>> listHotels(String city) {
        var result = hotelQueryFacade.listHotels(hotelMapper.toQuery(city));
        return ResponseEntity.ok(hotelMapper.toHotelResponse(result));
    }

    @Override
    public ResponseEntity<HotelResponse> getHotelDetails(Long hotelId) {
        var result = hotelQueryFacade.getHotelDetails(hotelId);
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<List<HotelServiceOfferingResponse>> listHotelServices(Long hotelId) {
        var result = hotelQueryFacade.listHotelServices(hotelId);
        return ResponseEntity.ok(hotelMapper.toServiceOfferingResponse(result));
    }

    @Override
    public ResponseEntity<List<RoomTypeAvailabilityCalendarDayResponse>> getRoomTypeAvailabilityCalendar(
            Long hotelId,
            Long roomTypeId,
            LocalDate from,
            LocalDate to
    ) {
        var query = availabilityMapper.toCalendarQuery(hotelId, roomTypeId, from, to);
        var result = getRoomTypeAvailabilityCalendarFacade.getAvailabilityCalendar(query);
        return ResponseEntity.ok(availabilityMapper.toCalendarResponse(result));
    }
}
