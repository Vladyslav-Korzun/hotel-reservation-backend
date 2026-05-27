package com.hotel.management.jpa.audit;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.stereotype.Component;

@Component
public class JpaAuditTrailAdapter implements AuditTrail {

    private final JpaAuditLogSpringDataRepository repository;
    private final ClockPort clockPort;

    JpaAuditTrailAdapter(JpaAuditLogSpringDataRepository repository, ClockPort clockPort) {
        this.repository = repository;
        this.clockPort = clockPort;
    }

    @Override
    public void record(
            AuthenticatedUser actor,
            AuditActionType action,
            AuditEntityType entityType,
            String entityId,
            String details
    ) {
        append(new AuditLogEntry(
                null,
                actor.userId(),
                actor.roles().stream().findFirst().orElse("UNKNOWN"),
                action,
                entityType,
                entityId,
                clockPort.now(),
                details
        ));
    }

    private void append(AuditLogEntry entry) {
        var entity = new JpaAuditLogEntity();
        entity.setId(entry.id());
        entity.setActorId(entry.actorId());
        entity.setActorRole(entry.actorRole());
        entity.setActionType(entry.actionType().name());
        entity.setEntityType(entry.entityType().name());
        entity.setEntityId(entry.entityId());
        entity.setTimestamp(entry.timestamp());
        entity.setDetails(entry.details());
        repository.save(entity);
    }
}
