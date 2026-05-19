package com.hotel.management.domain.service.reservation;

import java.util.List;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface ReservationFacade {

    CreateReservationResult createReservation(AuthenticatedUser actor, CreateReservationCommand command);

    CreateReservationResult createPublicReservation(CreatePublicReservationCommand command);

    CreateReservationResult createStaffReservation(AuthenticatedUser actor, CreateStaffReservationCommand command);

    List<GetReservationResult> listReservations(AuthenticatedUser actor, int limit);

    List<GetReservationResult> listMyReservations(AuthenticatedUser actor, int limit);

    GetReservationResult getReservation(AuthenticatedUser actor, String reservationId);

    void cancelReservation(AuthenticatedUser actor, String reservationId);
}
