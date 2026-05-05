package com.hotel.management;

import com.hotel.management.service.staff.StaffReservationFacade;
import com.hotel.management.service.staff.StaffReservationResult;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalStaffReservationFacade implements StaffReservationFacade {

    private final StaffReservationFacade delegate;

    public TransactionalStaffReservationFacade(StaffReservationFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public StaffReservationResult checkIn(String reservationId) {
        return delegate.checkIn(reservationId);
    }

    @Override
    @Transactional
    public StaffReservationResult checkOut(String reservationId) {
        return delegate.checkOut(reservationId);
    }
}
