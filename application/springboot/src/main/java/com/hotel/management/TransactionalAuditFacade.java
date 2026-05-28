package com.hotel.management;

import com.hotel.management.domain.audit.AuditLogResult;
import com.hotel.management.domain.service.audit.AuditFacade;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class TransactionalAuditFacade implements AuditFacade {

    private final AuditFacade delegate;

    public TransactionalAuditFacade(AuditFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResult> listAuditLog(AuthenticatedUser actor, int limit) {
        return delegate.listAuditLog(actor, limit);
    }
}
