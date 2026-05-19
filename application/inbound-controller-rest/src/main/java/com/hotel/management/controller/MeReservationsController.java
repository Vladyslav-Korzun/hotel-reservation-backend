package com.hotel.management.controller;

import com.hotel.management.api.MeApi;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MeReservationsController implements MeApi {

    private final ReservationFacade reservationFacade;
    private final ReservationMapper reservationMapper;

    public MeReservationsController(
            ReservationFacade reservationFacade,
            ReservationMapper reservationMapper
    ) {
        this.reservationFacade = reservationFacade;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public ResponseEntity<List<ReservationResponse>> listMyReservations(Integer limit) {
        var result = reservationFacade.listMyReservations(limit == null ? 100 : limit);
        return ResponseEntity.ok(reservationMapper.toResponse(result));
    }
}
