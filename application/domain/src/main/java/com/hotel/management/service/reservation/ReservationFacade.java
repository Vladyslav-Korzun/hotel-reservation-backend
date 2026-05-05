package com.hotel.management.service.reservation;

import java.util.List;

public interface ReservationFacade {

    CreateReservationResult createReservation(CreateReservationCommand command);

    List<GetReservationResult> listReservations(int limit);

    GetReservationResult getReservation(String reservationId);

    void cancelReservation(String reservationId);
}
