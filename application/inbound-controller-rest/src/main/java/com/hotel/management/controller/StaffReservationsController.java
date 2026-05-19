package com.hotel.management.controller;

import com.hotel.management.api.StaffApi;
import com.hotel.management.api.dto.CreateReservationResponse;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.api.dto.RoomOperationResponse;
import com.hotel.management.api.dto.StaffCreateReservationRequest;
import com.hotel.management.api.dto.UpdateRoomStatusRequest;
import com.hotel.management.service.reservation.ReservationFacade;
import com.hotel.management.service.staff.StaffReservationFacade;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.mapper.RoomMapper;
import com.hotel.management.service.room.RoomOperationsFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffReservationsController implements StaffApi {

    private final StaffReservationFacade staffReservationFacade;
    private final ReservationFacade reservationFacade;
    private final RoomOperationsFacade roomOperationsFacade;
    private final ReservationMapper reservationMapper;
    private final RoomMapper roomMapper;

    public StaffReservationsController(
            StaffReservationFacade staffReservationFacade,
            ReservationFacade reservationFacade,
            RoomOperationsFacade roomOperationsFacade,
            ReservationMapper reservationMapper,
            RoomMapper roomMapper
    ) {
        this.staffReservationFacade = staffReservationFacade;
        this.reservationFacade = reservationFacade;
        this.roomOperationsFacade = roomOperationsFacade;
        this.reservationMapper = reservationMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public ResponseEntity<CreateReservationResponse> createStaffReservation(
            StaffCreateReservationRequest staffCreateReservationRequest
    ) {
        var command = reservationMapper.toCommand(staffCreateReservationRequest);
        var result = reservationFacade.createStaffReservation(command);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> checkInReservation(String reservationId) {
        var result = staffReservationFacade.checkIn(reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> checkOutReservation(String reservationId) {
        var result = staffReservationFacade.checkOut(reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> markNoShowReservation(String reservationId) {
        var result = staffReservationFacade.markNoShow(reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<RoomOperationResponse> updateRoomStatus(
            Long roomId,
            UpdateRoomStatusRequest updateRoomStatusRequest
    ) {
        var command = roomMapper.toCommand(roomId, updateRoomStatusRequest);
        var result = roomOperationsFacade.updateRoomStatus(command);
        return ResponseEntity.ok(roomMapper.toResponse(result));
    }
}
