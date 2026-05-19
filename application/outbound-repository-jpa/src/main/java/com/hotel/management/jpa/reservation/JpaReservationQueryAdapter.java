package com.hotel.management.jpa.reservation;

import com.hotel.management.domain.reservation.ActiveReservationView;
import com.hotel.management.domain.reservation.BookedRoomTypePeriodView;
import com.hotel.management.domain.reservation.ReservationQueryPort;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.shared.value.StayPeriod;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    @Override
    public List<BookedRoomTypePeriodView> findBookedRoomTypePeriods(Long hotelId, Long roomTypeId, LocalDate from, LocalDate to) {
        if (hotelId == null || roomTypeId == null || from == null || to == null) {
            return List.of();
        }
        return springDataReservationRepository.findActiveRoomTypeOverlapping(
                        hotelId,
                        roomTypeId,
                        from,
                        to,
                        activeAvailabilityStatuses()
                ).stream()
                .map(entity -> new BookedRoomTypePeriodView(entity.getId(), entity.getCheckIn(), entity.getCheckOut()))
                .toList();
    }

    private List<String> activeAvailabilityStatuses() {
        return List.of(
                ReservationStatus.PENDING.name(),
                ReservationStatus.CONFIRMED.name(),
                ReservationStatus.CHECKED_IN.name()
        );
    }
}