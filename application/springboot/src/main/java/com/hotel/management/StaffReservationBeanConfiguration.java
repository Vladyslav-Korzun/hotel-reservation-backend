package com.hotel.management;

import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.stay.StayRepository;
import com.hotel.management.service.port.AuditLogPort;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.security.CurrentUserPort;
import com.hotel.management.service.staff.RoomAssignmentPort;
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
