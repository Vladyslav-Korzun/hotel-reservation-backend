package com.hotel.management.controller;

import com.hotel.management.api.AdminApi;
import com.hotel.management.api.dto.CreateHotelRequest;
import com.hotel.management.api.dto.CreateRoomRequest;
import com.hotel.management.api.dto.CreateRoomTypeRequest;
import com.hotel.management.api.dto.CreateServiceOfferingRequest;
import com.hotel.management.api.dto.HotelResponse;
import com.hotel.management.api.dto.HotelServiceOfferingResponse;
import com.hotel.management.api.dto.RoomResponse;
import com.hotel.management.api.dto.RoomTypeResponse;
import com.hotel.management.api.dto.UpdateHotelRequest;
import com.hotel.management.api.dto.UpdateRoomRequest;
import com.hotel.management.api.dto.UpdateRoomTypeRequest;
import com.hotel.management.api.dto.UpdateServiceOfferingRequest;
import com.hotel.management.mapper.HotelMapper;
import com.hotel.management.service.hotel.HotelAdministrationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotelAdministrationController implements AdminApi {

    private final HotelAdministrationFacade hotelAdministrationFacade;
    private final HotelMapper hotelMapper;

    public HotelAdministrationController(
            HotelAdministrationFacade hotelAdministrationFacade,
            HotelMapper hotelMapper
    ) {
        this.hotelAdministrationFacade = hotelAdministrationFacade;
        this.hotelMapper = hotelMapper;
    }

    @Override
    public ResponseEntity<HotelResponse> createHotel(CreateHotelRequest createHotelRequest) {
        var result = hotelAdministrationFacade.createHotel(hotelMapper.toCommand(createHotelRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<HotelResponse> updateHotel(Long hotelId, UpdateHotelRequest updateHotelRequest) {
        var result = hotelAdministrationFacade.updateHotel(hotelMapper.toCommand(hotelId, updateHotelRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomTypeResponse> createRoomType(CreateRoomTypeRequest createRoomTypeRequest) {
        var result = hotelAdministrationFacade.createRoomType(hotelMapper.toCommand(createRoomTypeRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomTypeResponse> updateRoomType(
            Long roomTypeId,
            UpdateRoomTypeRequest updateRoomTypeRequest
    ) {
        var result = hotelAdministrationFacade.updateRoomType(hotelMapper.toCommand(roomTypeId, updateRoomTypeRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomResponse> createRoom(CreateRoomRequest createRoomRequest) {
        var result = hotelAdministrationFacade.createRoom(hotelMapper.toCommand(createRoomRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomResponse> updateRoom(Long roomId, UpdateRoomRequest updateRoomRequest) {
        var result = hotelAdministrationFacade.updateRoom(hotelMapper.toCommand(roomId, updateRoomRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<HotelServiceOfferingResponse> createServiceOffering(
            Long hotelId,
            CreateServiceOfferingRequest createServiceOfferingRequest
    ) {
        var result = hotelAdministrationFacade.createServiceOffering(
                hotelMapper.toCommand(hotelId, createServiceOfferingRequest)
        );
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<HotelServiceOfferingResponse> updateServiceOffering(
            Long hotelId,
            Long serviceId,
            UpdateServiceOfferingRequest updateServiceOfferingRequest
    ) {
        var result = hotelAdministrationFacade.updateServiceOffering(
                hotelMapper.toCommand(hotelId, serviceId, updateServiceOfferingRequest)
        );
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<Void> deactivateServiceOffering(Long hotelId, Long serviceId) {
        hotelAdministrationFacade.deactivateServiceOffering(
                hotelMapper.toDeactivateServiceOfferingCommand(hotelId, serviceId)
        );
        return ResponseEntity.noContent().build();
    }
}
