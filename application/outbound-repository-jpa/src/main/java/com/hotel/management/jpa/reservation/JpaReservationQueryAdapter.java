package com.hotel.management.jpa.reservation;

import com.hotel.management.service.reservation.ActiveReservationView;
import com.hotel.management.service.reservation.ReservationQueryPort;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.shared.value.StayPeriod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaReservationQueryAdapter implements ReservationQueryPort {

    private final JpaReservationSpringDataRepository springDataReservationRepository;

    public JpaReservationQueryAdapter(JpaReservationSpringDataRepository springDataReservationRepository) {
        this.springDataReservationRepository = springDataReservationRepository;
    }

    @Override
    public List<ActiveReservationView> findActiveOverlapping(List<Long> hotelIds, StayPeriod stayPeriod) {
        if (hotelIds == null || hotelIds.isEmpty()) {
            return List.of();
        }
        return springDataReservationRepository.findActiveOverlapping(
                        hotelIds,
                        stayPeriod.checkIn(),
                        stayPeriod.checkOut(),
                        List.of(
                                ReservationStatus.PENDING.name(),
                                ReservationStatus.CONFIRMED.name(),
                                ReservationStatus.CHECKED_IN.name()
                        )
                ).stream()
                .map(entity -> new ActiveReservationView(entity.getHotelId(), entity.getRoomTypeId()))
                .toList();
    }
}
