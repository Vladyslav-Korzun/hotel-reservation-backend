package com.hotel.management.service.reservation;
import com.hotel.management.domain.shared.value.StayPeriod;

import java.time.LocalDate;
import java.util.List;

public interface ReservationQueryPort {

    List<ActiveReservationView> findActiveOverlapping(List<Long> hotelIds, StayPeriod stayPeriod);

    List<BookedRoomTypePeriodView> findBookedRoomTypePeriods(Long hotelId, Long roomTypeId, LocalDate from, LocalDate to);
}
