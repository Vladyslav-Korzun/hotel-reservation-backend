package com.hotel.management.jpa.audit;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaAuditLogSpringDataRepository extends JpaRepository<JpaAuditLogEntity, Long> {
}
