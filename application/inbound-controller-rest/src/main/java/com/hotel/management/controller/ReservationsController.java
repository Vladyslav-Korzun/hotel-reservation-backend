package com.hotel.management.controller;

import com.hotel.management.api.ReservationsApi;
import com.hotel.management.api.dto.CreateReservationRequest;
import com.hotel.management.api.dto.CreateReservationResponse;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.service.reservation.ReservationFacade;
import com.hotel.management.mapper.ReservationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReservationsController implements ReservationsApi {

    private final ReservationFacade reservationFacade;
    private final ReservationMapper reservationMapper;

    public ReservationsController(
            ReservationFacade reservationFacade,
            ReservationMapper reservationMapper
    ) {
        this.reservationFacade = reservationFacade;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public ResponseEntity<CreateReservationResponse> createReservation(CreateReservationRequest createReservationRequest) {
        var command = reservationMapper.toCommand(createReservationRequest);
        var result = reservationFacade.createReservation(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<List<ReservationResponse>> listReservations(Integer limit) {
        var result = reservationFacade.listReservations(limit == null ? 100 : limit);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> getReservation(String reservationId) {
        var result = reservationFacade.getReservation(reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<Void> cancelReservation(String reservationId) {
        reservationFacade.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
