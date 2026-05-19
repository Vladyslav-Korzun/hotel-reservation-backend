package com.hotel.management.domain.audit;

import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface AuditTrail {

    void record(
            AuthenticatedUser actor,
            AuditActionType action,
            AuditEntityType entityType,
            String entityId,
            String details
    );
}
