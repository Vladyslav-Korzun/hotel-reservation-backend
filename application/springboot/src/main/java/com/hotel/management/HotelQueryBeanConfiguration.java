package com.hotel.management;

import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.service.hotel.HotelQueryFacade;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.service.hotel.HotelQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotelQueryBeanConfiguration {

    @Bean
    HotelQueryResultMapper hotelQueryResultMapper() {
        return new HotelQueryResultMapper();
    }

    @Bean
    HotelQueryFacade hotelQueryFacade(
            HotelRepository hotelRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        return new HotelQueryService(
                hotelRepository,
                serviceOfferingRepository,
                hotelQueryResultMapper
        );
    }
}
