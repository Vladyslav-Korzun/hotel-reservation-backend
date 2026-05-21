package com.hotel.management;

import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.stay.StayRepository;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.reservation.RoomAssignmentPort;
import com.hotel.management.domain.service.staff.HotelScopePolicy;
import com.hotel.management.domain.service.staff.StaffReservationFacade;
import com.hotel.management.domain.service.mapper.StaffReservationResultMapper;
import com.hotel.management.domain.service.staff.StaffReservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaffReservationBeanConfiguration {

    @Bean
    StaffReservationResultMapper staffReservationResultMapper() {
        return new StaffReservationResultMapper();
    }

    @Bean
    StaffReservationFacade staffReservationFacade(
            ReservationRepository reservationRepository,
            ReservationLockPort reservationLockPort,
            RoomRepository roomRepository,
            StayRepository stayRepository,
            RoomAssignmentPort roomAssignmentPort,
            ClockPort clockPort,
            AuditTrail auditTrail,
            StaffReservationResultMapper staffReservationResultMapper,
            HotelScopePolicy hotelScopePolicy
    ) {
        var staffReservationService = new StaffReservationService(
                reservationRepository,
                reservationLockPort,
                roomRepository,
                stayRepository,
                roomAssignmentPort,
                clockPort,
                auditTrail,
                staffReservationResultMapper,
                hotelScopePolicy
        );
        return new TransactionalStaffReservationFacade(staffReservationService);
    }
}
