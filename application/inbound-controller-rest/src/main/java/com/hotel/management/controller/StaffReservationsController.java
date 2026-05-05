package com.hotel.management.controller;

import com.hotel.management.api.StaffApi;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.service.staff.StaffReservationFacade;
import com.hotel.management.mapper.ReservationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffReservationsController implements StaffApi {

    private final StaffReservationFacade staffReservationFacade;
    private final ReservationMapper reservationMapper;

    public StaffReservationsController(
            StaffReservationFacade staffReservationFacade,
            ReservationMapper reservationMapper
    ) {
        this.staffReservationFacade = staffReservationFacade;
        this.reservationMapper = reservationMapper;
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
}
