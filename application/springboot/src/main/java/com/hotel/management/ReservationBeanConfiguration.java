package com.hotel.management;

import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.service.reservation.ReservationFacade;
import com.hotel.management.service.reservation.ReservationQueryPort;
import com.hotel.management.service.reservation.ReservationService;
import com.hotel.management.service.reservation.RoomInventoryPort;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.security.CurrentUserPort;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomTypeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationBeanConfiguration {

    @Bean
    ReservationFacade reservationFacade(
            ReservationRepository reservationRepository,
            ReservationQueryPort reservationQueryPort,
            ReservationLockPort reservationLockPort,
            HotelRepository hotelRepository,
            RoomInventoryPort roomInventoryPort,
            RoomTypeRepository roomTypeRepository,
            ClockPort clockPort,
            CurrentUserPort currentUserPort,
            AccommodationPolicyValidator accommodationPolicyValidator
    ) {
        var reservationService = new ReservationService(
                reservationRepository,
                reservationQueryPort,
                reservationLockPort,
                hotelRepository,
                roomInventoryPort,
                roomTypeRepository,
                clockPort,
                currentUserPort,
                accommodationPolicyValidator
        );
        return new TransactionalReservationFacade(reservationService);
    }
}
