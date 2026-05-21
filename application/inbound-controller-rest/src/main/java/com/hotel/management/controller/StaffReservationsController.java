package com.hotel.management.controller;

import com.hotel.management.api.StaffApi;
import com.hotel.management.api.dto.CreateReservationResponse;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.api.dto.RoomOperationResponse;
import com.hotel.management.api.dto.StaffCreateReservationRequest;
import com.hotel.management.api.dto.UpdateRoomStatusRequest;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import com.hotel.management.domain.service.staff.StaffReservationFacade;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.mapper.RoomMapper;
import com.hotel.management.domain.service.room.RoomOperationsFacade;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StaffReservationsController implements StaffApi {

    private final StaffReservationFacade staffReservationFacade;
    private final ReservationFacade reservationFacade;
    private final RoomOperationsFacade roomOperationsFacade;
    private final CurrentUserPort currentUserPort;
    private final ReservationMapper reservationMapper;
    private final RoomMapper roomMapper;

    public StaffReservationsController(
            StaffReservationFacade staffReservationFacade,
            ReservationFacade reservationFacade,
            RoomOperationsFacade roomOperationsFacade,
            CurrentUserPort currentUserPort,
            ReservationMapper reservationMapper,
            RoomMapper roomMapper
    ) {
        this.staffReservationFacade = staffReservationFacade;
        this.reservationFacade = reservationFacade;
        this.roomOperationsFacade = roomOperationsFacade;
        this.currentUserPort = currentUserPort;
        this.reservationMapper = reservationMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public ResponseEntity<CreateReservationResponse> createStaffReservation(
            StaffCreateReservationRequest staffCreateReservationRequest
    ) {
        var actor = currentUserPort.getCurrentUser();
        var command = reservationMapper.toCommand(staffCreateReservationRequest);
        var result = reservationFacade.createStaffReservation(actor, command);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> checkInReservation(String reservationId) {
        var actor = currentUserPort.getCurrentUser();
        var result = staffReservationFacade.checkIn(actor, reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> checkOutReservation(String reservationId) {
        var actor = currentUserPort.getCurrentUser();
        var result = staffReservationFacade.checkOut(actor, reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> markNoShowReservation(String reservationId) {
        var actor = currentUserPort.getCurrentUser();
        var result = staffReservationFacade.markNoShow(actor, reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomOperationResponse> updateRoomStatus(
            Long roomId,
            UpdateRoomStatusRequest updateRoomStatusRequest
    ) {
        var actor = currentUserPort.getCurrentUser();
        var command = roomMapper.toCommand(roomId, updateRoomStatusRequest);
        var result = roomOperationsFacade.updateRoomStatus(actor, command);
        return ResponseEntity.ok(roomMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<List<RoomOperationResponse>> listRooms(Long hotelId) {
        var actor = currentUserPort.getCurrentUser();
        var result = roomOperationsFacade.listRooms(actor, hotelId);
        return ResponseEntity.ok(roomMapper.toResponse(result));
    }
}
