package com.hotel.management.domain.service.staff;
import com.hotel.management.domain.reservation.StaffReservationResult;

public interface StaffReservationFacade {

    StaffReservationResult checkIn(String reservationId);

    StaffReservationResult checkOut(String reservationId);

    StaffReservationResult markNoShow(String reservationId);
}