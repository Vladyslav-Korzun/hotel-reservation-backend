package com.hotel.management;

import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.service.mapper.RoomOperationResultMapper;
import com.hotel.management.domain.service.room.RoomOperationsFacade;
import com.hotel.management.domain.service.room.RoomOperationsService;
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
            AuditTrail auditTrail,
            RoomOperationResultMapper roomOperationResultMapper
    ) {
        var roomOperationsService = new RoomOperationsService(
                roomRepository,
                auditTrail,
                roomOperationResultMapper
        );
        return new TransactionalRoomOperationsFacade(roomOperationsService);
    }
}
