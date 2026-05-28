package com.hotel.management.domain.service.audit;

import com.hotel.management.domain.audit.AuditLogResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

import java.util.List;

public interface AuditFacade {

    List<AuditLogResult> listAuditLog(AuthenticatedUser actor, int limit);
}
