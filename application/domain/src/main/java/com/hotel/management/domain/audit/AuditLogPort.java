package com.hotel.management.domain.audit;

import com.hotel.management.domain.audit.AuditLogEntry;

public interface AuditLogPort {

    void append(AuditLogEntry entry);
}
