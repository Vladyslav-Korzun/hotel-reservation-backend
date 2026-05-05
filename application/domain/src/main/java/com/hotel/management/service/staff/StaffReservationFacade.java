package com.hotel.management.service.staff;

public interface StaffReservationFacade {

    StaffReservationResult checkIn(String reservationId);

    StaffReservationResult checkOut(String reservationId);
}
