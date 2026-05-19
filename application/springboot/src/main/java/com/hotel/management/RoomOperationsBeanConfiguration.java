package com.hotel.management;

import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.service.mapper.RoomOperationResultMapper;
import com.hotel.management.domain.service.room.RoomOperationsFacade;
import com.hotel.management.domain.service.room.RoomOperationsService;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoomOperationsBeanConfiguration {

    @Bean
    RoomOperationResultMapper roomOperationResultMapper() {
        return new RoomOperationResultMapper();
    }

    @Bean
    RoomOperationsFacade roomOperationsFacade(
            RoomRepository roomRepository,
            CurrentUserPort currentUserPort,
            AuditTrail auditTrail,
            RoomOperationResultMapper roomOperationResultMapper
    ) {
        var roomOperationsService = new RoomOperationsService(
                roomRepository,
                currentUserPort,
                auditTrail,
                roomOperationResultMapper
        );
        return new TransactionalRoomOperationsFacade(roomOperationsService);
    }
}
