package com.hotel.reservation.jpa;

import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.reservation.ReservationRepository;
import com.hotel.reservation.reservation.ReservationStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final ReservationSpringDataRepository springDataReservationRepository;

    public JpaReservationRepositoryAdapter(ReservationSpringDataRepository springDataReservationRepository) {
        this.springDataReservationRepository = springDataReservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        var entity = new ReservationJpaEntity();
        entity.setId(reservation.id());
        entity.setHotelId(reservation.hotelId());
        entity.setRoomTypeId(reservation.roomTypeId());
        entity.setCheckIn(reservation.checkIn());
        entity.setCheckOut(reservation.checkOut());
        entity.setGuestCount(reservation.guestCount());
        entity.setStatus(reservation.status().name());
        entity.setCreatedAt(reservation.createdAt());
        entity.setCancelledAt(reservation.cancelledAt());
        entity.setCreatedBy(reservation.createdBy());

        var savedEntity = springDataReservationRepository.save(entity);
        return Reservation.rehydrate(
                savedEntity.getId(),
                savedEntity.getHotelId(),
                savedEntity.getRoomTypeId(),
                savedEntity.getCheckIn(),
                savedEntity.getCheckOut(),
                savedEntity.getGuestCount(),
                ReservationStatus.valueOf(savedEntity.getStatus()),
                savedEntity.getCreatedAt(),
                savedEntity.getCancelledAt(),
                savedEntity.getCreatedBy()
        );
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        return springDataReservationRepository.findById(reservationId)
                .map(savedEntity -> Reservation.rehydrate(
                        savedEntity.getId(),
                        savedEntity.getHotelId(),
                        savedEntity.getRoomTypeId(),
                        savedEntity.getCheckIn(),
                        savedEntity.getCheckOut(),
                        savedEntity.getGuestCount(),
                        ReservationStatus.valueOf(savedEntity.getStatus()),
                        savedEntity.getCreatedAt(),
                        savedEntity.getCancelledAt(),
                        savedEntity.getCreatedBy()
                ));
    }
}
