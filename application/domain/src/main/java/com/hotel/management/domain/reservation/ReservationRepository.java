package com.hotel.management.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(String reservationId);

    List<Reservation> findAll(int limit);
}
