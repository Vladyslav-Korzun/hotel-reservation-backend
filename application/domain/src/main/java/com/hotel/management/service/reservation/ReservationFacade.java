package com.hotel.management.service.reservation;

import java.util.List;

public interface ReservationFacade {

    CreateReservationResult createReservation(CreateReservationCommand command);

    CreateReservationResult createPublicReservation(CreatePublicReservationCommand command);

    CreateReservationResult createStaffReservation(CreateStaffReservationCommand command);

    List<GetReservationResult> listReservations(int limit);

    List<GetReservationResult> listMyReservations(int limit);

    GetReservationResult getReservation(String reservationId);

    void cancelReservation(String reservationId);
}
