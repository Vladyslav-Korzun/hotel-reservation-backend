package com.hotel.management.jpa.stay;

import com.hotel.management.domain.stay.Stay;
import com.hotel.management.domain.stay.StayRepository;
import com.hotel.management.domain.stay.StayStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaStayRepositoryAdapter implements StayRepository {

    private final JpaStaySpringDataRepository repository;

    JpaStayRepositoryAdapter(JpaStaySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Stay save(Stay stay) {
        return toDomain(repository.save(toEntity(stay)));
    }

    @Override
    public Optional<Stay> findActiveByReservationId(String reservationId) {
        return repository.findByReservationIdAndStatus(reservationId, StayStatus.ACTIVE.name())
                .map(this::toDomain);
    }

    private Stay toDomain(JpaStayEntity entity) {
        return Stay.rehydrate(
                entity.getId(),
                entity.getReservationId(),
                entity.getRoomId(),
                entity.getCheckedInAt(),
                entity.getCheckedOutAt(),
                StayStatus.valueOf(entity.getStatus())
        );
    }

    private JpaStayEntity toEntity(Stay stay) {
        var entity = new JpaStayEntity();
        entity.setId(stay.id());
        entity.setReservationId(stay.reservationId());
        entity.setRoomId(stay.roomId());
        entity.setCheckedInAt(stay.checkedInAt());
        entity.setCheckedOutAt(stay.checkedOutAt());
        entity.setStatus(stay.status().name());
        return entity;
    }
}
