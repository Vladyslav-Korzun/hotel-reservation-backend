package com.hotel.management.jpa.staff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface JpaStaffSpringDataRepository extends JpaRepository<JpaStaffEntity, Long> {

    Optional<JpaStaffEntity> findByExternalId(String externalId);
}
