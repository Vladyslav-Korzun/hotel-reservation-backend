package com.hotel.management;

import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.reservation.ReservationFactory;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.domain.audit.AuditLogPort;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.shared.NotificationPort;
import com.hotel.management.service.reservation.ReservationCreationValidator;
import com.hotel.management.service.reservation.ReservationFacade;
import com.hotel.management.service.reservation.ReservationPricingCalculator;
import com.hotel.management.domain.reservation.ReservationQueryPort;
import com.hotel.management.service.reservation.ReservationResultMapper;
import com.hotel.management.service.reservation.ReservationService;
import com.hotel.management.domain.reservation.RoomInventoryPort;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationBeanConfiguration {

    @Bean
    ReservationFactory reservationFactory() {
        return new ReservationFactory();
    }

    @Bean
    ReservationCreationValidator reservationCreationValidator(
            ReservationQueryPort reservationQueryPort,
            HotelRepository hotelRepository,
            GuestRepository guestRepository,
            RoomInventoryPort roomInventoryPort,
            RoomTypeRepository roomTypeRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            ClockPort clockPort,
            AccommodationPolicyValidator accommodationPolicyValidator
    ) {
        return new ReservationCreationValidator(
                reservationQueryPort,
                hotelRepository,
                guestRepository,
                roomInventoryPort,
                roomTypeRepository,
                serviceOfferingRepository,
                clockPort,
                accommodationPolicyValidator
        );
    }

    @Bean
    ReservationPricingCalculator reservationPricingCalculator() {
        return new ReservationPricingCalculator();
    }

    @Bean
    ReservationResultMapper reservationResultMapper() {
        return new ReservationResultMapper();
    }

    @Bean
    ReservationFacade reservationFacade(
            ReservationRepository reservationRepository,
            GuestRepository guestRepository,
            ReservationLockPort reservationLockPort,
            ClockPort clockPort,
            CurrentUserPort currentUserPort,
            ReservationCreationValidator reservationCreationValidator,
            ReservationFactory reservationFactory,
            ReservationPricingCalculator reservationPricingCalculator,
            ReservationResultMapper reservationResultMapper,
            AuditLogPort auditLogPort,
            NotificationPort notificationPort
    ) {
        var reservationService = new ReservationService(
                reservationRepository,
                guestRepository,
                reservationLockPort,
                clockPort,
                currentUserPort,
                reservationCreationValidator,
                reservationFactory,
                reservationPricingCalculator,
                reservationResultMapper,
                auditLogPort,
                notificationPort
        );
        return new TransactionalReservationFacade(reservationService);
    }
}