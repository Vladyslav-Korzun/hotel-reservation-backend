package com.hotel.management;

import com.hotel.management.domain.service.availability.SearchAvailabilityFacade;
import com.hotel.management.domain.service.availability.GetRoomTypeAvailabilityCalendarFacade;
import com.hotel.management.domain.service.availability.GetRoomTypeAvailabilityCalendarService;
import com.hotel.management.domain.service.availability.SearchAvailabilityService;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.domain.reservation.ReservationQueryPort;
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

    @Bean
    GetRoomTypeAvailabilityCalendarFacade getRoomTypeAvailabilityCalendarFacade(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            RoomRepository roomRepository,
            ReservationQueryPort reservationQueryPort
    ) {
        return new GetRoomTypeAvailabilityCalendarService(
                hotelRepository,
                roomTypeRepository,
                roomRepository,
                reservationQueryPort
        );
    }
}