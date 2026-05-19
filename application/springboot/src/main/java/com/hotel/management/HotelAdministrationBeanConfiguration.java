package com.hotel.management;

import com.hotel.management.domain.hotel.HotelFactory;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.RoomFactory;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomTypeFactory;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOfferingFactory;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.service.hotel.HotelAdministrationFacade;
import com.hotel.management.domain.service.hotel.HotelAdministrationService;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotelAdministrationBeanConfiguration {

    @Bean
    HotelFactory hotelFactory() {
        return new HotelFactory();
    }

    @Bean
    RoomTypeFactory roomTypeFactory() {
        return new RoomTypeFactory();
    }

    @Bean
    RoomFactory roomFactory() {
        return new RoomFactory();
    }

    @Bean
    ServiceOfferingFactory serviceOfferingFactory() {
        return new ServiceOfferingFactory();
    }

    @Bean
    HotelAdministrationFacade hotelAdministrationFacade(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            RoomRepository roomRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            CurrentUserPort currentUserPort,
            AuditTrail auditTrail,
            HotelFactory hotelFactory,
            RoomTypeFactory roomTypeFactory,
            RoomFactory roomFactory,
            ServiceOfferingFactory serviceOfferingFactory,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        var hotelAdministrationService = new HotelAdministrationService(
                hotelRepository,
                roomTypeRepository,
                roomRepository,
                serviceOfferingRepository,
                currentUserPort,
                auditTrail,
                hotelFactory,
                roomTypeFactory,
                roomFactory,
                serviceOfferingFactory,
                hotelQueryResultMapper
        );
        return new TransactionalHotelAdministrationFacade(hotelAdministrationService);
    }
}
