package com.hotel.management.domain.audit;

import com.hotel.management.domain.shared.security.AuthenticatedUser;

import java.util.List;

public interface AuditTrail {

    void record(
            AuthenticatedUser actor,
            AuditActionType action,
            AuditEntityType entityType,
            String entityId,
            String details
    );

    List<AuditLogEntry> findRecent(int limit);
}
