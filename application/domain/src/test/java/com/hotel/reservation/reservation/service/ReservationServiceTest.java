package com.hotel.reservation.reservation.service;

import com.hotel.reservation.reservation.Reservation;
import com.hotel.reservation.reservation.ReservationRepository;
import com.hotel.reservation.reservation.ReservationStatus;
import com.hotel.reservation.shared.exception.ForbiddenException;
import com.hotel.reservation.shared.exception.NotFoundException;
import com.hotel.reservation.shared.exception.ValidationException;
import com.hotel.reservation.shared.port.ClockPort;
import com.hotel.reservation.shared.port.CurrentUserPort;
import com.hotel.reservation.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private ClockPort clockPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @InjectMocks
    private ReservationService facade;

    @Test
    void shouldCreateReservationForAuthenticatedUser() {
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ValidationException.class, () -> facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-10"),
                2
        )));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnReservationById() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                2,
                ReservationStatus.PENDING,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        );
        when(repository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));
        var result = facade.getReservation("reservation-1");

        assertEquals("reservation-1", result.reservationId());
        assertEquals(1L, result.hotelId());
    }

    @Test
    void shouldThrowWhenReservationNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> facade.getReservation("missing"));
    }

    @Test
    void shouldCancelReservationWhenFound() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                2,
                ReservationStatus.PENDING,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        );
        when(repository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-04T10:00:00Z"));
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        facade.cancelReservation("reservation-1");

        verify(repository).save(any(Reservation.class));
    }

    @Test
    void shouldThrowWhenCancellingMissingReservation() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> facade.cancelReservation("missing"));
    }

    @Test
    void shouldRejectAccessToReservationOwnedByAnotherUser() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                2,
                ReservationStatus.PENDING,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        );
        when(repository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-999", Set.of("GUEST")));

        assertThrows(ForbiddenException.class, () -> facade.getReservation("reservation-1"));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectCancellingAlreadyCancelledReservation() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                2,
                ReservationStatus.CANCELLED,
                Instant.parse("2026-04-03T12:00:00Z"),
                Instant.parse("2026-04-04T10:00:00Z"),
                "guest-123"
        );
        when(repository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ValidationException.class, () -> facade.cancelReservation("reservation-1"));
        verify(repository, never()).save(any());
    }
}
