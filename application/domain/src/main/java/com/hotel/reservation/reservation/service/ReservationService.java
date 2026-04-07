package com.hotel.reservation.reservation.service;

import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.reservation.ReservationRepository;
import com.hotel.reservation.shared.exception.NotFoundException;
import com.hotel.reservation.shared.exception.ValidationException;
import com.hotel.reservation.shared.port.ClockPort;
import com.hotel.reservation.shared.port.CurrentUserPort;
import com.hotel.reservation.shared.security.AuthenticatedUser;

import java.util.UUID;

public class ReservationService implements ReservationFacade {

    private final ReservationRepository reservationRepository;
    private final ClockPort clockPort;
    private final CurrentUserPort currentUserPort;

    public ReservationService(
            ReservationRepository reservationRepository,
            ClockPort clockPort,
            CurrentUserPort currentUserPort
    ) {
        this.reservationRepository = reservationRepository;
        this.clockPort = clockPort;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public CreateReservationResult createReservation(CreateReservationCommand command) {
        requireCommand(command);

        var currentUser = currentUserPort.getCurrentUser();
        var reservation = Reservation.createPending(
                UUID.randomUUID().toString(),
                command.hotelId(),
                command.roomTypeId(),
                command.checkIn(),
                command.checkOut(),
                command.guestCount(),
                clockPort.now(),
                currentUser.userId()
        );

        var savedReservation = reservationRepository.save(reservation);
        return new CreateReservationResult(
                savedReservation.id(),
                savedReservation.hotelId(),
                savedReservation.roomTypeId(),
                savedReservation.checkIn(),
                savedReservation.checkOut(),
                savedReservation.guestCount(),
                savedReservation.status().name(),
                savedReservation.createdAt(),
                savedReservation.cancelledAt(),
                savedReservation.createdBy()
        );
    }

    @Override
    public GetReservationResult getReservation(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        var reservation = loadReservation(reservationId);
        assertCanAccess(reservation, currentUser);

        return new GetReservationResult(
                reservation.id(),
                reservation.hotelId(),
                reservation.roomTypeId(),
                reservation.checkIn(),
                reservation.checkOut(),
                reservation.guestCount(),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.cancelledAt(),
                reservation.createdBy()
        );
    }

    @Override
    public void cancelReservation(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        var reservation = loadReservation(reservationId);
        assertCanAccess(reservation, currentUser);
        reservationRepository.save(reservation.cancel(clockPort.now()));
    }

    private Reservation loadReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ValidationException("reservationId is required");
        }

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
    }

    private void assertCanAccess(Reservation reservation, AuthenticatedUser currentUser) {
        if (!currentUser.isAdmin()) {
            reservation.assertOwnedBy(currentUser.userId());
        }
    }

    private void requireCommand(CreateReservationCommand command) {
        if (command == null) {
            throw new ValidationException("reservation command is required");
        }
    }
}
