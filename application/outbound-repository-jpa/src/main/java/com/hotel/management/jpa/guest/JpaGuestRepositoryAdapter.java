package com.hotel.management.jpa.guest;

import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.shared.value.EmailAddress;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaGuestRepositoryAdapter implements GuestRepository {

    private final JpaGuestSpringDataRepository repository;
    private final EntityManager entityManager;

    JpaGuestRepositoryAdapter(JpaGuestSpringDataRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Guest> findById(Long guestId) {
        return repository.findById(guestId).map(this::toDomain);
    }

    @Override
    public Optional<Guest> findByEmail(EmailAddress email) {
        return repository.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public Optional<Guest> findByKeycloakId(String keycloakId) {
        return repository.findByKeycloakId(keycloakId).map(this::toDomain);
    }

    @Override
    public Guest save(Guest guest) {
        Guest guestWithId = guest.id() == null ? withGeneratedId(guest) : guest;
        return toDomain(repository.save(toEntity(guestWithId)));
    }

    private Guest withGeneratedId(Guest guest) {
        Number nextId = (Number) entityManager
                .createNativeQuery("select nextval('guest_id_seq')")
                .getSingleResult();
        return new Guest(
                nextId.longValue(),
                guest.firstName(),
                guest.lastName(),
                guest.email(),
                guest.phone(),
                guest.keycloakId()
        );
    }

    private Guest toDomain(JpaGuestEntity entity) {
        return new Guest(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhone(),
                entity.getKeycloakId()
        );
    }

    private JpaGuestEntity toEntity(Guest guest) {
        var entity = new JpaGuestEntity();
        entity.setId(guest.id());
        entity.setFirstName(guest.firstName());
        entity.setLastName(guest.lastName());
        entity.setEmail(guest.email().value());
        entity.setPhone(guest.phone());
        entity.setKeycloakId(guest.keycloakId());
        return entity;
    }
}
