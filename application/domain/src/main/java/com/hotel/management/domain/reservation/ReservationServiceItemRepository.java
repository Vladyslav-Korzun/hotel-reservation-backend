package com.hotel.management.domain.reservation;

import java.util.List;

public interface ReservationServiceItemRepository {

    List<ReservationServiceItem> findByReservationId(String reservationId);

    void replaceForReservation(String reservationId, List<ReservationServiceItem> serviceItems);
}
