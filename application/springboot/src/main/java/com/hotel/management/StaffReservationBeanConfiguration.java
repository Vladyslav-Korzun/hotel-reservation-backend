package com.hotel.management;

import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.staff.RoomAssignmentPort;
import com.hotel.management.service.security.CurrentUserPort;
import com.hotel.management.service.staff.StaffReservationFacade;
import com.hotel.management.service.staff.StaffReservationService;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaffReservationBeanConfiguration {

    @Bean
    StaffReservationFacade staffReservationFacade(
            ReservationRepository reservationRepository,
            ReservationLockPort reservationLockPort,
            RoomRepository roomRepository,
            RoomAssignmentPort roomAssignmentPort,
            CurrentUserPort currentUserPort,
            ClockPort clockPort
    ) {
        var staffReservationService = new StaffReservationService(
                reservationRepository,
                reservationLockPort,
                roomRepository,
                roomAssignmentPort,
                currentUserPort,
                clockPort
        );
        return new TransactionalStaffReservationFacade(staffReservationService);
    }
}
