package com.hotel.management;

import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.stay.StayRepository;
import com.hotel.management.domain.audit.AuditLogPort;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import com.hotel.management.domain.reservation.RoomAssignmentPort;
import com.hotel.management.service.staff.StaffReservationFacade;
import com.hotel.management.service.staff.StaffReservationResultMapper;
import com.hotel.management.service.staff.StaffReservationService;
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
            CurrentUserPort currentUserPort,
            ClockPort clockPort,
            AuditLogPort auditLogPort,
            StaffReservationResultMapper staffReservationResultMapper
    ) {
        var staffReservationService = new StaffReservationService(
                reservationRepository,
                reservationLockPort,
                roomRepository,
                stayRepository,
                roomAssignmentPort,
                currentUserPort,
                clockPort,
                auditLogPort,
                staffReservationResultMapper
        );
        return new TransactionalStaffReservationFacade(staffReservationService);
    }
}