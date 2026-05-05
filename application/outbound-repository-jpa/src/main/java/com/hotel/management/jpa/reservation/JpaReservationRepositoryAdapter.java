package com.hotel.management.jpa.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.jpa.shared.JsonColumnCodec;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationSpringDataRepository springDataReservationRepository;

    public JpaReservationRepositoryAdapter(JpaReservationSpringDataRepository springDataReservationRepository) {
        this.springDataReservationRepository = springDataReservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        var entity = new JpaReservationEntity();
        entity.setId(reservation.id());
        entity.setHotelId(reservation.hotelId());
        entity.setRoomId(reservation.roomId());
        entity.setRoomTypeId(reservation.roomTypeId());
        entity.setCheckIn(reservation.checkIn());
        entity.setCheckOut(reservation.checkOut());
        entity.setAdultsCount(reservation.accommodationParty().guests().adults());
        entity.setChildrenAgesJson(JsonColumnCodec.write(reservation.accommodationParty().guests().childrenAges()));
        entity.setPetsJson(JsonColumnCodec.write(reservation.accommodationParty().pets()));
        entity.setStatus(reservation.status().name());
        entity.setCreatedAt(reservation.createdAt());
        entity.setCancelledAt(reservation.cancelledAt());
        entity.setCreatedBy(reservation.createdBy());

        var savedEntity = springDataReservationRepository.save(entity);
        return Reservation.rehydrate(
                savedEntity.getId(),
                savedEntity.getHotelId(),
                savedEntity.getRoomId(),
                savedEntity.getRoomTypeId(),
                savedEntity.getCheckIn(),
                savedEntity.getCheckOut(),
                toAccommodationParty(savedEntity),
                ReservationStatus.valueOf(savedEntity.getStatus()),
                savedEntity.getCreatedAt(),
                savedEntity.getCancelledAt(),
                savedEntity.getCreatedBy()
        );
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        return springDataReservationRepository.findById(reservationId)
                .map(this::toDomain);
    }

    @Override
    public List<Reservation> findAll(int limit) {
        return springDataReservationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    private Reservation toDomain(JpaReservationEntity savedEntity) {
        return Reservation.rehydrate(
            savedEntity.getId(),
            savedEntity.getHotelId(),
            savedEntity.getRoomId(),
            savedEntity.getRoomTypeId(),
                savedEntity.getCheckIn(),
                savedEntity.getCheckOut(),
                toAccommodationParty(savedEntity),
                ReservationStatus.valueOf(savedEntity.getStatus()),
                savedEntity.getCreatedAt(),
                savedEntity.getCancelledAt(),
                savedEntity.getCreatedBy()
        );
    }

    private AccommodationParty toAccommodationParty(JpaReservationEntity entity) {
        return new AccommodationParty(
                new GuestComposition(
                        entity.getAdultsCount(),
                        JsonColumnCodec.read(entity.getChildrenAgesJson(), new TypeReference<List<Integer>>() { }, List.of())
                ),
                JsonColumnCodec.read(entity.getPetsJson(), new TypeReference<List<PetDetails>>() { }, List.of())
        );
    }
}
