package com.hotel.reservation;

import com.hotel.reservation.reservation.ReservationRepository;
import com.hotel.reservation.reservation.service.ReservationFacade;
import com.hotel.reservation.reservation.service.ReservationService;
import com.hotel.reservation.shared.port.ClockPort;
import com.hotel.reservation.shared.port.CurrentUserPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class ReservationBeanConfiguration {

    @Bean
    ReservationFacade reservationFacade(
            ReservationRepository reservationRepository,
            ClockPort clockPort,
            CurrentUserPort currentUserPort
    ) {
        return new ReservationService(reservationRepository, clockPort, currentUserPort);
    }

    @Bean
    ClockPort clockPort() {
        return Instant::now;
    }
}
