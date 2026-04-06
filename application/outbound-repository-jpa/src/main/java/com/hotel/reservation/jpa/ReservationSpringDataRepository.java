package com.hotel.reservation.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSpringDataRepository extends JpaRepository<ReservationJpaEntity, String> {
}
