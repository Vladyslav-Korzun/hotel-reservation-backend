package com.hotel.management;

import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.reservation.ReservationFactory;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.service.reservation.ReservationCreationValidator;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import com.hotel.management.domain.service.reservation.ReservationPricingCalculator;
import com.hotel.management.domain.reservation.ReservationQueryPort;
import com.hotel.management.domain.service.mapper.ReservationResultMapper;
import com.hotel.management.domain.service.reservation.ReservationService;
import com.hotel.management.domain.service.staff.HotelScopePolicy;
import com.hotel.management.domain.reservation.RoomInventoryPort;
import com.hotel.management.domain.reservation.ReservationLockPort;
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
            ReservationCreationValidator reservationCreationValidator,
            ReservationFactory reservationFactory,
            ReservationPricingCalculator reservationPricingCalculator,
            ReservationResultMapper reservationResultMapper,
            AuditTrail auditTrail,
            HotelScopePolicy hotelScopePolicy
    ) {
        var reservationService = new ReservationService(
                reservationRepository,
                guestRepository,
                reservationLockPort,
                clockPort,
                reservationCreationValidator,
                reservationFactory,
                reservationPricingCalculator,
                reservationResultMapper,
                auditTrail,
                hotelScopePolicy
        );
        return new TransactionalReservationFacade(reservationService);
    }
}
