package com.hotel.reservation.reservation.service;

import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.reservation.ReservationRepository;
import com.hotel.reservation.reservation.ReservationStatus;
import com.hotel.reservation.shared.exception.NotFoundException;
import com.hotel.reservation.shared.exception.ValidationException;
import com.hotel.reservation.shared.port.ClockPort;
import com.hotel.reservation.shared.port.CurrentUserPort;

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
        validate(command);

        var currentUser = currentUserPort.getCurrentUser();
        var reservation = new Reservation(
                UUID.randomUUID().toString(),
                command.hotelId(),
                command.roomTypeId(),
                command.checkIn(),
                command.checkOut(),
                command.guestCount(),
                ReservationStatus.PENDING,
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
                savedReservation.createdBy()
        );
    }

    @Override
    public GetReservationResult getReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ValidationException("reservationId is required");
        }

        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        return new GetReservationResult(
                reservation.id(),
                reservation.hotelId(),
                reservation.roomTypeId(),
                reservation.checkIn(),
                reservation.checkOut(),
                reservation.guestCount(),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.createdBy()
        );
    }

    @Override
    public void deleteReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ValidationException("reservationId is required");
        }

        if (reservationRepository.findById(reservationId).isEmpty()) {
            throw new NotFoundException("Reservation not found: " + reservationId);
        }

        reservationRepository.deleteById(reservationId);
    }

    private void validate(CreateReservationCommand command) {
        if (command.hotelId() == null) {
            throw new ValidationException("hotelId is required");
        }
        if (command.roomTypeId() == null) {
            throw new ValidationException("roomTypeId is required");
        }
        if (command.checkIn() == null || command.checkOut() == null) {
            throw new ValidationException("checkIn and checkOut are required");
        }
        if (!command.checkOut().isAfter(command.checkIn())) {
            throw new ValidationException("checkOut must be after checkIn");
        }
        if (command.guestCount() <= 0) {
            throw new ValidationException("guestCount must be greater than zero");
        }
    }
}
