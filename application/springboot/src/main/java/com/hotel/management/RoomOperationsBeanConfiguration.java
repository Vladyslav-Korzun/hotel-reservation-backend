package com.hotel.management;

import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.service.port.AuditLogPort;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.room.RoomOperationResultMapper;
import com.hotel.management.service.room.RoomOperationsFacade;
import com.hotel.management.service.room.RoomOperationsService;
import com.hotel.management.service.security.CurrentUserPort;
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
            ClockPort clockPort,
            AuditLogPort auditLogPort,
            RoomOperationResultMapper roomOperationResultMapper
    ) {
        var roomOperationsService = new RoomOperationsService(
                roomRepository,
                currentUserPort,
                clockPort,
                auditLogPort,
                roomOperationResultMapper
        );
        return new TransactionalRoomOperationsFacade(roomOperationsService);
    }
}
