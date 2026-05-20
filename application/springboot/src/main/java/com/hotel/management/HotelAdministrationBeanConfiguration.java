package com.hotel.management;

import com.hotel.management.domain.hotel.HotelFactory;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.RoomFactory;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomTypeFactory;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOfferingFactory;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.service.hotel.HotelFacade;
import com.hotel.management.domain.service.hotel.HotelService;
import com.hotel.management.domain.service.hotel.RoomAdministrationFacade;
import com.hotel.management.domain.service.hotel.RoomAdministrationService;
import com.hotel.management.domain.service.hotel.RoomTypeFacade;
import com.hotel.management.domain.service.hotel.RoomTypeService;
import com.hotel.management.domain.service.hotel.ServiceOfferingFacade;
import com.hotel.management.domain.service.hotel.ServiceOfferingService;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.audit.AuditTrail;
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
    HotelFacade hotelFacade(
            HotelRepository hotelRepository,
            AuditTrail auditTrail,
            HotelFactory hotelFactory,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        var service = new HotelService(
                hotelRepository,
                hotelFactory,
                auditTrail,
                hotelQueryResultMapper
        );
        return new TransactionalHotelFacade(service);
    }

    @Bean
    RoomTypeFacade roomTypeFacade(
            RoomTypeRepository roomTypeRepository,
            HotelRepository hotelRepository,
            AuditTrail auditTrail,
            RoomTypeFactory roomTypeFactory,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        var service = new RoomTypeService(
                roomTypeRepository,
                hotelRepository,
                roomTypeFactory,
                auditTrail,
                hotelQueryResultMapper
        );
        return new TransactionalRoomTypeFacade(service);
    }

    @Bean
    RoomAdministrationFacade roomAdministrationFacade(
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            HotelRepository hotelRepository,
            AuditTrail auditTrail,
            RoomFactory roomFactory,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        var service = new RoomAdministrationService(
                roomRepository,
                roomTypeRepository,
                hotelRepository,
                roomFactory,
                auditTrail,
                hotelQueryResultMapper
        );
        return new TransactionalRoomAdministrationFacade(service);
    }

    @Bean
    ServiceOfferingFacade serviceOfferingFacade(
            ServiceOfferingRepository serviceOfferingRepository,
            HotelRepository hotelRepository,
            AuditTrail auditTrail,
            ServiceOfferingFactory serviceOfferingFactory,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        var service = new ServiceOfferingService(
                serviceOfferingRepository,
                hotelRepository,
                serviceOfferingFactory,
                auditTrail,
                hotelQueryResultMapper
        );
        return new TransactionalServiceOfferingFacade(service);
    }
}
