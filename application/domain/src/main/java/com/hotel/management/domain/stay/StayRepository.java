package com.hotel.management.domain.stay;

import java.util.Optional;

public interface StayRepository {

    Stay save(Stay stay);

    Optional<Stay> findActiveByReservationId(String reservationId);
}
