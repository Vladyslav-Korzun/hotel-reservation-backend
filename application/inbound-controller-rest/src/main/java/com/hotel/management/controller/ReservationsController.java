package com.hotel.management.controller;

import com.hotel.management.api.PublicApi;
import com.hotel.management.api.ReservationsApi;
import com.hotel.management.api.dto.CreateReservationRequest;
import com.hotel.management.api.dto.CreateReservationResponse;
import com.hotel.management.api.dto.PublicCreateReservationRequest;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.domain.shared.exception.ConflictException;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import com.hotel.management.mapper.ReservationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReservationsController implements ReservationsApi, PublicApi {

    private final ReservationFacade reservationFacade;
    private final CurrentUserPort currentUserPort;
    private final ReservationMapper reservationMapper;

    public ReservationsController(
            ReservationFacade reservationFacade,
            CurrentUserPort currentUserPort,
            ReservationMapper reservationMapper
    ) {
        this.reservationFacade = reservationFacade;
        this.currentUserPort = currentUserPort;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public ResponseEntity<CreateReservationResponse> createReservation(CreateReservationRequest createReservationRequest) {
        var actor = currentUserPort.getCurrentUser();
        var command = reservationMapper.toCommand(createReservationRequest);
        var result = reservationFacade.createReservation(actor, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<CreateReservationResponse> createPublicReservation(
            PublicCreateReservationRequest publicCreateReservationRequest
    ) {
        assertAnonymousCheckout();
        var command = reservationMapper.toCommand(publicCreateReservationRequest);
        var result = reservationFacade.createPublicReservation(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<List<ReservationResponse>> listReservations(Integer limit) {
        var actor = currentUserPort.getCurrentUser();
        var result = reservationFacade.listReservations(actor, limit == null ? 100 : limit);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ReservationResponse> getReservation(String reservationId) {
        var actor = currentUserPort.getCurrentUser();
        var result = reservationFacade.getReservation(actor, reservationId);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }

    @Override
    public ResponseEntity<Void> cancelReservation(String reservationId) {
        var actor = currentUserPort.getCurrentUser();
        reservationFacade.cancelReservation(actor, reservationId);
        return ResponseEntity.noContent().build();
    }

    private void assertAnonymousCheckout() {
        if (!currentUserPort.isAnonymous()) {
            throw new ConflictException("Authenticated users must use POST /reservations");
        }
    }
}
