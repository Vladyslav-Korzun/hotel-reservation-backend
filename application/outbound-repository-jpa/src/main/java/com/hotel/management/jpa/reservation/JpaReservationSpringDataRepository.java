package com.hotel.management.jpa.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface JpaReservationSpringDataRepository extends JpaRepository<JpaReservationEntity, String> {

    List<JpaReservationEntity> findAllByOrderByCreatedAtDesc();

    List<JpaReservationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select reservation
            from JpaReservationEntity reservation
            where reservation.id = :reservationId
            """)
    java.util.Optional<JpaReservationEntity> findByIdForUpdate(@Param("reservationId") String reservationId);

    @Query("""
            select reservation
            from JpaReservationEntity reservation
            where reservation.hotelId in :hotelIds
              and reservation.status in :statuses
              and reservation.checkIn < :checkOut
              and reservation.checkOut > :checkIn
            """)
    List<JpaReservationEntity> findActiveOverlapping(
            @Param("hotelIds") List<Long> hotelIds,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("statuses") List<String> statuses
    );
}
