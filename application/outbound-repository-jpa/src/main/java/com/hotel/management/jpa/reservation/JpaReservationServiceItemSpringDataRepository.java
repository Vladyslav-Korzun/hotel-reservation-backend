package com.hotel.management.jpa.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface JpaReservationServiceItemSpringDataRepository extends JpaRepository<JpaReservationServiceItemEntity, Long> {

    List<JpaReservationServiceItemEntity> findByReservationId(String reservationId);

    void deleteByReservationId(String reservationId);
}
