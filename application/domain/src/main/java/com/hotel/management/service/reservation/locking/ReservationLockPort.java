package com.hotel.management.service.reservation.locking;

import com.hotel.management.domain.reservation.Reservation;

import java.util.Optional;

public interface ReservationLockPort {

    Optional<Reservation> findReservationForChange(String reservationId);
}
