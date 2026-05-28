package com.hotel.management;

import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.service.audit.AuditFacade;
import com.hotel.management.domain.service.audit.AuditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditBeanConfiguration {

    @Bean
    AuditFacade auditFacade(AuditTrail auditTrail) {
        return new TransactionalAuditFacade(new AuditService(auditTrail));
    }
}
