package com.hotel.management.jpa.guest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface JpaGuestSpringDataRepository extends JpaRepository<JpaGuestEntity, Long> {

    Optional<JpaGuestEntity> findByEmail(String email);

    Optional<JpaGuestEntity> findByKeycloakId(String keycloakId);
}
