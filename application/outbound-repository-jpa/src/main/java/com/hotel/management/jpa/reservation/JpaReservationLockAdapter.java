package com.hotel.management.jpa.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.jpa.shared.JsonColumnCodec;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaReservationLockAdapter implements ReservationLockPort {

    private final JpaReservationSpringDataRepository springDataReservationRepository;

    public JpaReservationLockAdapter(JpaReservationSpringDataRepository springDataReservationRepository) {
        this.springDataReservationRepository = springDataReservationRepository;
    }

    @Override
    public Optional<Reservation> findReservationForChange(String reservationId) {
        return springDataReservationRepository.findByIdForUpdate(reservationId)
                .map(this::toDomain);
    }

    private Reservation toDomain(JpaReservationEntity entity) {
        return Reservation.rehydrate(
                entity.getId(),
                entity.getHotelId(),
                entity.getRoomId(),
                entity.getRoomTypeId(),
                entity.getCheckIn(),
                entity.getCheckOut(),
                new AccommodationParty(
                        new GuestComposition(
                                entity.getAdultsCount(),
                                JsonColumnCodec.read(entity.getChildrenAgesJson(), new TypeReference<List<Integer>>() { }, List.of())
                        ),
                        JsonColumnCodec.read(entity.getPetsJson(), new TypeReference<List<PetDetails>>() { }, List.of())
                ),
                ReservationStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getCancelledAt(),
                entity.getCreatedBy()
        );
    }
}
