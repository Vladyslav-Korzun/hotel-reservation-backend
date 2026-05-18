package com.hotel.management.jpa.stay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface JpaStaySpringDataRepository extends JpaRepository<JpaStayEntity, Long> {

    Optional<JpaStayEntity> findByReservationIdAndStatus(String reservationId, String status);
}
