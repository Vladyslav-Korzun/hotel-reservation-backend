package com.hotel.reservation.controller;

import com.hotel.reservation.api.ReservationsApi;
import com.hotel.reservation.api.dto.CreateReservationRequest;
import com.hotel.reservation.api.dto.CreateReservationResponse;
import com.hotel.reservation.api.dto.ReservationResponse;
import com.hotel.reservation.mapper.ReservationMapper;
import com.hotel.reservation.reservation.service.ReservationFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ReservationResponse> getReservation(String reservationId) {
        var result = reservationFacade.getReservation(reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<Void> deleteReservation(String reservationId) {
        reservationFacade.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
