package com.hotel.management.domain.service.reservation;

import java.util.List;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.reservation.GetReservationResult;

public interface ReservationFacade {

    CreateReservationResult createReservation(CreateReservationCommand command);

    CreateReservationResult createPublicReservation(CreatePublicReservationCommand command);

    CreateReservationResult createStaffReservation(CreateStaffReservationCommand command);

    List<GetReservationResult> listReservations(int limit);

    List<GetReservationResult> listMyReservations(int limit);

    GetReservationResult getReservation(String reservationId);

    void cancelReservation(String reservationId);
}