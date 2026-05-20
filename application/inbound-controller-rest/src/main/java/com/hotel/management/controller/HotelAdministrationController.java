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
import com.hotel.management.domain.service.hotel.HotelFacade;
import com.hotel.management.domain.service.hotel.RoomAdministrationFacade;
import com.hotel.management.domain.service.hotel.RoomTypeFacade;
import com.hotel.management.domain.service.hotel.ServiceOfferingFacade;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotelAdministrationController implements AdminApi {

    private final HotelFacade hotelFacade;
    private final RoomTypeFacade roomTypeFacade;
    private final RoomAdministrationFacade roomAdministrationFacade;
    private final ServiceOfferingFacade serviceOfferingFacade;
    private final CurrentUserPort currentUserPort;
    private final HotelMapper hotelMapper;

    public HotelAdministrationController(
            HotelFacade hotelFacade,
            RoomTypeFacade roomTypeFacade,
            RoomAdministrationFacade roomAdministrationFacade,
            ServiceOfferingFacade serviceOfferingFacade,
            CurrentUserPort currentUserPort,
            HotelMapper hotelMapper
    ) {
        this.hotelFacade = hotelFacade;
        this.roomTypeFacade = roomTypeFacade;
        this.roomAdministrationFacade = roomAdministrationFacade;
        this.serviceOfferingFacade = serviceOfferingFacade;
        this.currentUserPort = currentUserPort;
        this.hotelMapper = hotelMapper;
    }

    @Override
    public ResponseEntity<HotelResponse> createHotel(CreateHotelRequest createHotelRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelFacade.createHotel(actor, hotelMapper.toCommand(createHotelRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<HotelResponse> updateHotel(Long hotelId, UpdateHotelRequest updateHotelRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = hotelFacade.updateHotel(actor, hotelMapper.toCommand(hotelId, updateHotelRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomTypeResponse> createRoomType(CreateRoomTypeRequest createRoomTypeRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = roomTypeFacade.createRoomType(actor, hotelMapper.toCommand(createRoomTypeRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomTypeResponse> updateRoomType(
            Long roomTypeId,
            UpdateRoomTypeRequest updateRoomTypeRequest
    ) {
        var actor = currentUserPort.getCurrentUser();
        var result = roomTypeFacade.updateRoomType(actor, hotelMapper.toCommand(roomTypeId, updateRoomTypeRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomResponse> createRoom(CreateRoomRequest createRoomRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = roomAdministrationFacade.createRoom(actor, hotelMapper.toCommand(createRoomRequest));
        return ResponseEntity.status(201).body(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomResponse> updateRoom(Long roomId, UpdateRoomRequest updateRoomRequest) {
        var actor = currentUserPort.getCurrentUser();
        var result = roomAdministrationFacade.updateRoom(actor, hotelMapper.toCommand(roomId, updateRoomRequest));
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<HotelServiceOfferingResponse> createServiceOffering(
            Long hotelId,
            CreateServiceOfferingRequest createServiceOfferingRequest
    ) {
        var actor = currentUserPort.getCurrentUser();
        var result = serviceOfferingFacade.createServiceOffering(
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
        var result = serviceOfferingFacade.updateServiceOffering(
                actor,
                hotelMapper.toCommand(hotelId, serviceId, updateServiceOfferingRequest)
        );
        return ResponseEntity.ok(hotelMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<Void> deactivateServiceOffering(Long hotelId, Long serviceId) {
        var actor = currentUserPort.getCurrentUser();
        serviceOfferingFacade.deactivateServiceOffering(
                actor,
                hotelMapper.toDeactivateServiceOfferingCommand(hotelId, serviceId)
        );
        return ResponseEntity.noContent().build();
    }
}
