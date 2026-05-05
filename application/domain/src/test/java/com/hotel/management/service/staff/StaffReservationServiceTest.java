package com.hotel.management.service.staff;

import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.service.exception.ForbiddenException;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.security.AuthenticatedUser;
import com.hotel.management.service.security.CurrentUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationLockPort reservationLockPort;

    @Mock
    private RoomAssignmentPort roomAssignmentPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldCheckInReservationAndOccupyRoom() {
        var service = service();
        var reservation = reservation("reservation-1", null, ReservationStatus.PENDING);
        var room = room(100L, RoomStatus.AVAILABLE);

        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));
        when(reservationLockPort.findReservationForChange("reservation-1")).thenReturn(Optional.of(reservation));
        when(roomAssignmentPort.findAvailableRoomForCheckIn(1L, 2L)).thenReturn(Optional.of(room));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.checkIn("reservation-1");

        assertEquals("CHECKED_IN", result.status());
        assertEquals(100L, result.roomId());

        var savedReservation = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(savedReservation.capture());
        assertEquals(ReservationStatus.CHECKED_IN, savedReservation.getValue().status());
        assertEquals(100L, savedReservation.getValue().roomId());

        var savedRoom = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(savedRoom.capture());
        assertEquals(RoomStatus.OCCUPIED, savedRoom.getValue().status());
    }

    @Test
    void shouldCheckOutReservationAndMoveRoomToCleaning() {
        var service = service();
        var reservation = reservation("reservation-1", 100L, ReservationStatus.CHECKED_IN);
        var room = room(100L, RoomStatus.OCCUPIED);

        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(reservationLockPort.findReservationForChange("reservation-1")).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(100L)).thenReturn(Optional.of(room));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.checkOut("reservation-1");

        assertEquals("CHECKED_OUT", result.status());
        assertEquals(100L, result.roomId());

        var savedRoom = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(savedRoom.capture());
        assertEquals(RoomStatus.CLEANING, savedRoom.getValue().status());
    }

    @Test
    void shouldRejectGuestCheckIn() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-1", Set.of("GUEST")));

        assertThrows(ForbiddenException.class, () -> service.checkIn("reservation-1"));
        verify(reservationRepository, never()).findById(any());
    }

    @Test
    void shouldRejectCheckInWithoutAvailableRoom() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));
        when(reservationLockPort.findReservationForChange("reservation-1"))
                .thenReturn(Optional.of(reservation("reservation-1", null, ReservationStatus.PENDING)));
        when(roomAssignmentPort.findAvailableRoomForCheckIn(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> service.checkIn("reservation-1"));
        verify(reservationRepository, never()).save(any());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void shouldRejectCheckInBeforeReservationDate() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-09T10:00:00Z"));
        when(reservationLockPort.findReservationForChange("reservation-1"))
                .thenReturn(Optional.of(reservation("reservation-1", null, ReservationStatus.PENDING)));

        assertThrows(ValidationException.class, () -> service.checkIn("reservation-1"));
        verify(roomAssignmentPort, never()).findAvailableRoomForCheckIn(anyLong(), anyLong());
    }

    @Test
    void shouldRejectCheckInAfterReservationCheckOutDate() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-12T10:00:00Z"));
        when(reservationLockPort.findReservationForChange("reservation-1"))
                .thenReturn(Optional.of(reservation("reservation-1", null, ReservationStatus.PENDING)));

        assertThrows(ValidationException.class, () -> service.checkIn("reservation-1"));
        verify(roomAssignmentPort, never()).findAvailableRoomForCheckIn(anyLong(), anyLong());
    }

    private StaffReservationService service() {
        return new StaffReservationService(
                reservationRepository,
                reservationLockPort,
                roomRepository,
                roomAssignmentPort,
                currentUserPort,
                clockPort
        );
    }

    private static Reservation reservation(String id, Long roomId, ReservationStatus status) {
        return Reservation.rehydrate(
                id,
                1L,
                roomId,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                new AccommodationParty(new GuestComposition(2, List.of()), List.of()),
                status,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-1"
        );
    }

    private static Room room(Long id, RoomStatus status) {
        return new Room(id, 1L, "101", 2L, 2, status);
    }
}
