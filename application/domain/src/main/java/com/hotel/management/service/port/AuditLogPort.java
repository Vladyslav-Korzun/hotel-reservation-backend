package com.hotel.management.service.port;

import com.hotel.management.domain.audit.AuditLogEntry;

public interface AuditLogPort {

    void append(AuditLogEntry entry);
}
