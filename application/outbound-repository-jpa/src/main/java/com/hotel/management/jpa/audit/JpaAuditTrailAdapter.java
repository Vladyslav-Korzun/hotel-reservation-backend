package com.hotel.management.jpa.audit;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    public List<AuditLogEntry> findRecent(int limit) {
        return repository.findAll(
                PageRequest.of(0, limit, Sort.by("timestamp").descending())
        ).stream().map(this::toDomain).toList();
    }

    private AuditLogEntry toDomain(JpaAuditLogEntity entity) {
        return new AuditLogEntry(
                entity.getId(),
                entity.getActorId(),
                entity.getActorRole(),
                AuditActionType.valueOf(entity.getActionType()),
                AuditEntityType.valueOf(entity.getEntityType()),
                entity.getEntityId(),
                entity.getTimestamp(),
                entity.getDetails()
        );
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
