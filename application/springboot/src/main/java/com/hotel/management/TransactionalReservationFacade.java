package com.hotel.management;

import com.hotel.management.domain.service.reservation.CreateReservationCommand;
import com.hotel.management.domain.service.reservation.CreatePublicReservationCommand;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.service.reservation.CreateStaffReservationCommand;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class TransactionalReservationFacade implements ReservationFacade {

    private final ReservationFacade delegate;

    public TransactionalReservationFacade(ReservationFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public CreateReservationResult createReservation(AuthenticatedUser actor, CreateReservationCommand command) {
        return delegate.createReservation(actor, command);
    }

    @Override
    @Transactional
    public CreateReservationResult createPublicReservation(CreatePublicReservationCommand command) {
        return delegate.createPublicReservation(command);
    }

    @Override
    @Transactional
    public CreateReservationResult createStaffReservation(AuthenticatedUser actor, CreateStaffReservationCommand command) {
        return delegate.createStaffReservation(actor, command);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetReservationResult> listReservations(AuthenticatedUser actor, int limit) {
        return delegate.listReservations(actor, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetReservationResult> listMyReservations(AuthenticatedUser actor, int limit) {
        return delegate.listMyReservations(actor, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public GetReservationResult getReservation(AuthenticatedUser actor, String reservationId) {
        return delegate.getReservation(actor, reservationId);
    }

    @Override
    @Transactional
    public void cancelReservation(AuthenticatedUser actor, String reservationId) {
        delegate.cancelReservation(actor, reservationId);
    }
}
