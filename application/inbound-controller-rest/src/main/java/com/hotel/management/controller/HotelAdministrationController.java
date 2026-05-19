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
import com.hotel.management.domain.service.hotel.HotelAdministrationFacade;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotelAdministrationController implements AdminApi {

    private final HotelAdministrationFacade hotelAdministrationFacade;
    private final CurrentUserPort currentUserPort;
    private final HotelMapper hotelMapper;

    public HotelAdministrationController(
            HotelAdministrationFacade hotelAdministrationFacade,
            CurrentUserPort currentUserPort,
            HotelMapper hotelMapper
    ) {
        this.hotelAdministrationFacade = hotelAdministrationFacade;
        this.currentUserPort = currentUserPort;
        this.hotelMapper = hotelMapper;
    }

    @Override
    public ResponseEntity<HotelResponse> createHotel(CreateHotelRequest createHotelRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.createHotel(actor, hotelMapper.toCommand(createHotelRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<HotelResponse> updateHotel(Long hotelId, UpdateHotelRequest updateHotelRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.updateHotel(actor, hotelMapper.toCommand(hotelId, updateHotelRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomTypeResponse> createRoomType(CreateRoomTypeRequest createRoomTypeRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.createRoomType(actor, hotelMapper.toCommand(createRoomTypeRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomTypeResponse> updateRoomType(
            Long roomTypeId,
            UpdateRoomTypeRequest updateRoomTypeRequest
    ) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.updateRoomType(actor, hotelMapper.toCommand(roomTypeId, updateRoomTypeRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomResponse> createRoom(CreateRoomRequest createRoomRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.createRoom(actor, hotelMapper.toCommand(createRoomRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomResponse> updateRoom(Long roomId, UpdateRoomRequest updateRoomRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.updateRoom(actor, hotelMapper.toCommand(roomId, updateRoomRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<HotelServiceOfferingResponse> createServiceOffering(
            Long hotelId,
            CreateServiceOfferingRequest createServiceOfferingRequest
    ) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.createServiceOffering(
                actor,
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
        var actor = currentUserPort.getCurrentUser();
        var result = hotelAdministrationFacade.updateServiceOffering(
                actor,
                hotelMapper.toCommand(hotelId, serviceId, updateServiceOfferingRequest)
        );
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<Void> deactivateServiceOffering(Long hotelId, Long serviceId) {
        var actor = currentUserPort.getCurrentUser();
        hotelAdministrationFacade.deactivateServiceOffering(
                actor,
                hotelMapper.toDeactivateServiceOfferingCommand(hotelId, serviceId)
        );
        return ResponseEntity.noContent().build();
    }
}
