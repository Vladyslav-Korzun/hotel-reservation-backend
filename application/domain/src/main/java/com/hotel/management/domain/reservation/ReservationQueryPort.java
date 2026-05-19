package com.hotel.management.domain.reservation;
import com.hotel.management.domain.shared.value.StayPeriod;

import java.time.LocalDate;
import java.util.List;
import com.hotel.management.domain.reservation.ActiveReservationView;
import com.hotel.management.domain.reservation.BookedRoomTypePeriodView;

public interface ReservationQueryPort {

    List<ActiveReservationView> findActiveOverlapping(List<Long> hotelIds, StayPeriod stayPeriod);

    List<BookedRoomTypePeriodView> findBookedRoomTypePeriods(Long hotelId, Long roomTypeId, LocalDate from, LocalDate to);
}