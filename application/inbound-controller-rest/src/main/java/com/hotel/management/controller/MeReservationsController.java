package com.hotel.management.controller;

import com.hotel.management.api.MeApi;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MeReservationsController implements MeApi {

    private final ReservationFacade reservationFacade;
    private final CurrentUserPort currentUserPort;
    private final ReservationMapper reservationMapper;

    public MeReservationsController(
            ReservationFacade reservationFacade,
            CurrentUserPort currentUserPort,
            ReservationMapper reservationMapper
    ) {
        this.reservationFacade = reservationFacade;
        this.currentUserPort = currentUserPort;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public ResponseEntity<List<ReservationResponse>> listMyReservations(Integer limit) {
        var actor = currentUserPort.getCurrentUser();
        var result = reservationFacade.listMyReservations(actor, limit == null ? 100 : limit);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }
}
