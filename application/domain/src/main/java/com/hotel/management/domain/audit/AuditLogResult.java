package com.hotel.management.domain.audit;

import java.time.Instant;

public record AuditLogResult(
        Long id,
        String actorId,
        String actorRole,
        String actionType,
        String entityType,
        String entityId,
        Instant timestamp,
        String details
) {
}
