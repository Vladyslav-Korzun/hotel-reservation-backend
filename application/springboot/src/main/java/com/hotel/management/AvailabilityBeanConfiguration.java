package com.hotel.management;

import com.hotel.management.service.availability.SearchAvailabilityFacade;
import com.hotel.management.service.availability.SearchAvailabilityService;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.service.reservation.ReservationQueryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvailabilityBeanConfiguration {

    @Bean
    SearchAvailabilityFacade searchAvailabilityFacade(
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            ReservationQueryPort reservationQueryPort,
            AccommodationPolicyValidator accommodationPolicyValidator
    ) {
        return new SearchAvailabilityService(
                hotelRepository,
                roomRepository,
                roomTypeRepository,
                reservationQueryPort,
                accommodationPolicyValidator
        );
    }
}
