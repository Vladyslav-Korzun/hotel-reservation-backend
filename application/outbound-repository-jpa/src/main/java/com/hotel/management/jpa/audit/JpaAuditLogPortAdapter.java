package com.hotel.management.jpa.audit;

import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.service.port.AuditLogPort;
import org.springframework.stereotype.Component;

@Component
public class JpaAuditLogPortAdapter implements AuditLogPort {

    private final JpaAuditLogSpringDataRepository repository;

    public JpaAuditLogPortAdapter(JpaAuditLogSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void append(AuditLogEntry entry) {
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
