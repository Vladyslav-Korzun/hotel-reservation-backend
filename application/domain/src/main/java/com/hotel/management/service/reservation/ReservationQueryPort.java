package com.hotel.management.service.reservation;
import com.hotel.management.domain.shared.value.StayPeriod;

import java.util.List;

public interface ReservationQueryPort {

    List<ActiveReservationView> findActiveOverlapping(List<Long> hotelIds, StayPeriod stayPeriod);
}
