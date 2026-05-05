package com.hotel.management;

import com.hotel.management.service.reservation.CreateReservationCommand;
import com.hotel.management.service.reservation.CreateReservationResult;
import com.hotel.management.service.reservation.GetReservationResult;
import com.hotel.management.service.reservation.ReservationFacade;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class TransactionalReservationFacade implements ReservationFacade {

    private final ReservationFacade delegate;

    public TransactionalReservationFacade(ReservationFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public CreateReservationResult createReservation(CreateReservationCommand command) {
        return delegate.createReservation(command);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetReservationResult> listReservations(int limit) {
        return delegate.listReservations(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public GetReservationResult getReservation(String reservationId) {
        return delegate.getReservation(reservationId);
    }

    @Override
    @Transactional
    public void cancelReservation(String reservationId) {
        delegate.cancelReservation(reservationId);
    }
}
