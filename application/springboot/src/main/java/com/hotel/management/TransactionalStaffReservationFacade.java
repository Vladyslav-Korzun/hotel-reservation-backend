package com.hotel.management;

import com.hotel.management.domain.service.staff.StaffReservationFacade;
import com.hotel.management.domain.reservation.StaffReservationResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalStaffReservationFacade implements StaffReservationFacade {

    private final StaffReservationFacade delegate;

    public TransactionalStaffReservationFacade(StaffReservationFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public StaffReservationResult checkIn(AuthenticatedUser actor, String reservationId) {
        return delegate.checkIn(actor, reservationId);
    }

    @Override
    @Transactional
    public StaffReservationResult checkOut(AuthenticatedUser actor, String reservationId) {
        return delegate.checkOut(actor, reservationId);
    }

    @Override
    @Transactional
    public StaffReservationResult markNoShow(AuthenticatedUser actor, String reservationId) {
        return delegate.markNoShow(actor, reservationId);
    }
}
