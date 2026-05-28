package com.hotel.management.domain.service.audit;

import com.hotel.management.domain.audit.AuditLogResult;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

import java.util.List;

public class AuditService implements AuditFacade {

    private final AuditTrail auditTrail;

    public AuditService(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    @Override
    public List<AuditLogResult> listAuditLog(AuthenticatedUser actor, int limit) {
        if (actor == null) {
            throw new ForbiddenException("authentication is required");
        }
        actor.requireAdmin();
        int resolvedLimit = limit > 0 ? limit : 50;
        return auditTrail.findRecent(resolvedLimit).stream()
                .map(e -> new AuditLogResult(
                        e.id(),
                        e.actorId(),
                        e.actorRole(),
                        e.actionType().name(),
                        e.entityType().name(),
                        e.entityId(),
                        e.timestamp(),
                        e.details()
                ))
                .toList();
    }
}
