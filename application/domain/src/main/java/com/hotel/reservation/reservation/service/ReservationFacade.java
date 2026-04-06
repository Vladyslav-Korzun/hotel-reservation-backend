package com.hotel.reservation.reservation.service;

public interface ReservationFacade {

    CreateReservationResult createReservation(CreateReservationCommand command);

    GetReservationResult getReservation(String reservationId);

    void deleteReservation(String reservationId);
}
