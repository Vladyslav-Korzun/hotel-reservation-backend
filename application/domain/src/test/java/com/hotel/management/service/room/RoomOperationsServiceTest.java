package com.hotel.management.service.room;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.service.exception.ForbiddenException;
import com.hotel.management.service.port.AuditLogPort;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.security.AuthenticatedUser;
import com.hotel.management.service.security.CurrentUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomOperationsServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private ClockPort clockPort;

    @Mock
    private AuditLogPort auditLogPort;

    @Test
    void shouldUpdateRoomStatusAndWriteAudit() {
        var service = service();
        var room = room(RoomStatus.CLEANING);

        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.updateRoomStatus(new UpdateRoomStatusCommand(1L, RoomStatus.AVAILABLE));

        assertEquals("AVAILABLE", result.status());

        var savedRoom = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(savedRoom.capture());
        assertEquals(RoomStatus.AVAILABLE, savedRoom.getValue().status());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.UPDATE_ROOM_STATUS, auditEntry.getValue().actionType());
        assertEquals(AuditEntityType.ROOM, auditEntry.getValue().entityType());
        assertEquals("1", auditEntry.getValue().entityId());
    }

    @Test
    void shouldRejectGuestUser() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-1", Set.of("GUEST")));

        assertThrows(
                ForbiddenException.class,
                () -> service.updateRoomStatus(new UpdateRoomStatusCommand(1L, RoomStatus.CLEANING))
        );
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void shouldRejectMissingRoomId() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(new UpdateRoomStatusCommand(null, RoomStatus.CLEANING))
        );
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void shouldRejectMissingStatus() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(new UpdateRoomStatusCommand(1L, null))
        );
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void shouldRejectUnknownRoom() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.updateRoomStatus(new UpdateRoomStatusCommand(1L, RoomStatus.CLEANING))
        );
        verify(roomRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    @Test
    void shouldRejectManualOccupiedStatus() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(RoomStatus.AVAILABLE)));

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(new UpdateRoomStatusCommand(1L, RoomStatus.OCCUPIED))
        );
        verify(roomRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    @Test
    void shouldRejectOccupiedRoomMovedToMaintenance() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(RoomStatus.OCCUPIED)));

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(new UpdateRoomStatusCommand(1L, RoomStatus.MAINTENANCE))
        );
        verify(roomRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    private RoomOperationsService service() {
        return new RoomOperationsService(
                roomRepository,
                currentUserPort,
                clockPort,
                auditLogPort,
                new RoomOperationResultMapper()
        );
    }

    private static Room room(RoomStatus status) {
        return new Room(1L, 10L, "101", 20L, 2, status);
    }
}
