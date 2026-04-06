package com.hotel.reservation.reservation.service;

import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.reservation.ReservationRepository;
import com.hotel.reservation.reservation.ReservationStatus;
import com.hotel.reservation.shared.exception.NotFoundException;
import com.hotel.reservation.shared.exception.ValidationException;
import com.hotel.reservation.shared.port.ClockPort;
import com.hotel.reservation.shared.port.CurrentUserPort;
import com.hotel.reservation.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationServiceTest {

    @Test
    void shouldCreateReservationForAuthenticatedUser() {
        ReservationRepository repository = new ReservationRepository() {
            @Override
            public Reservation save(Reservation reservation) {
                return reservation;
            }

            @Override
            public Optional<Reservation> findById(String reservationId) {
                return Optional.empty();
            }

            @Override
            public void deleteById(String reservationId) {
            }
        };
        ClockPort clockPort = () -> Instant.parse("2026-04-03T12:00:00Z");
        CurrentUserPort currentUserPort = () -> new AuthenticatedUser("guest-123", Set.of("GUEST"));

        var facade = new ReservationService(repository, clockPort, currentUserPort);
        var result = facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                2
        ));

        assertEquals("PENDING", result.status());
        assertEquals(1L, result.hotelId());
        assertEquals("guest-123", result.createdBy());
        assertEquals(Instant.parse("2026-04-03T12:00:00Z"), result.createdAt());
    }

    @Test
    void shouldRejectInvalidStayPeriod() {
        ReservationRepository repository = new ReservationRepository() {
            @Override
            public Reservation save(Reservation reservation) {
                return reservation;
            }

            @Override
            public Optional<Reservation> findById(String reservationId) {
                return Optional.empty();
            }

            @Override
            public void deleteById(String reservationId) {
            }
        };
        ClockPort clockPort = Instant::now;
        CurrentUserPort currentUserPort = () -> new AuthenticatedUser("guest-123", Set.of("GUEST"));

        var facade = new ReservationService(repository, clockPort, currentUserPort);

        assertThrows(ValidationException.class, () -> facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-10"),
                2
        )));
    }

    @Test
    void shouldReturnReservationById() {
        Reservation reservation = new Reservation(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                2,
                ReservationStatus.PENDING,
                Instant.parse("2026-04-03T12:00:00Z"),
                "guest-123"
        );

        ReservationRepository repository = new ReservationRepository() {
            @Override
            public Reservation save(Reservation value) {
                return value;
            }

            @Override
            public Optional<Reservation> findById(String reservationId) {
                return Optional.of(reservation);
            }

            @Override
            public void deleteById(String reservationId) {
            }
        };
        ClockPort clockPort = Instant::now;
        CurrentUserPort currentUserPort = () -> new AuthenticatedUser("guest-123", Set.of("GUEST"));

        var facade = new ReservationService(repository, clockPort, currentUserPort);
        var result = facade.getReservation("reservation-1");

        assertEquals("reservation-1", result.reservationId());
        assertEquals(1L, result.hotelId());
    }

    @Test
    void shouldThrowWhenReservationNotFound() {
        ReservationRepository repository = new ReservationRepository() {
            @Override
            public Reservation save(Reservation reservation) {
                return reservation;
            }

            @Override
            public Optional<Reservation> findById(String reservationId) {
                return Optional.empty();
            }

            @Override
            public void deleteById(String reservationId) {
            }
        };
        ClockPort clockPort = Instant::now;
        CurrentUserPort currentUserPort = () -> new AuthenticatedUser("guest-123", Set.of("GUEST"));

        var facade = new ReservationService(repository, clockPort, currentUserPort);

        assertThrows(NotFoundException.class, () -> facade.getReservation("missing"));
    }

    @Test
    void shouldDeleteReservationWhenFound() {
        Reservation reservation = new Reservation(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                2,
                ReservationStatus.PENDING,
                Instant.parse("2026-04-03T12:00:00Z"),
                "guest-123"
        );

        boolean[] deleted = {false};
        ReservationRepository repository = new ReservationRepository() {
            @Override
            public Reservation save(Reservation value) {
                return value;
            }

            @Override
            public Optional<Reservation> findById(String reservationId) {
                return Optional.of(reservation);
            }

            @Override
            public void deleteById(String reservationId) {
                deleted[0] = true;
            }
        };
        ClockPort clockPort = Instant::now;
        CurrentUserPort currentUserPort = () -> new AuthenticatedUser("guest-123", Set.of("GUEST"));

        var facade = new ReservationService(repository, clockPort, currentUserPort);

        facade.deleteReservation("reservation-1");

        assertEquals(true, deleted[0]);
    }

    @Test
    void shouldThrowWhenDeletingMissingReservation() {
        ReservationRepository repository = new ReservationRepository() {
            @Override
            public Reservation save(Reservation reservation) {
                return reservation;
            }

            @Override
            public Optional<Reservation> findById(String reservationId) {
                return Optional.empty();
            }

            @Override
            public void deleteById(String reservationId) {
            }
        };
        ClockPort clockPort = Instant::now;
        CurrentUserPort currentUserPort = () -> new AuthenticatedUser("guest-123", Set.of("GUEST"));

        var facade = new ReservationService(repository, clockPort, currentUserPort);

        assertThrows(NotFoundException.class, () -> facade.deleteReservation("missing"));
    }
}
