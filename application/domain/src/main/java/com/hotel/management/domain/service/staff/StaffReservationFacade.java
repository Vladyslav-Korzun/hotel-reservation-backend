package com.hotel.management.domain.service.staff;
import com.hotel.management.domain.reservation.StaffReservationResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface StaffReservationFacade {

    StaffReservationResult checkIn(AuthenticatedUser actor, String reservationId);

    StaffReservationResult checkOut(AuthenticatedUser actor, String reservationId);

    StaffReservationResult markNoShow(AuthenticatedUser actor, String reservationId);
}
