package com.hotel.management.domain.service.room;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hotel.management.domain.service.mapper.RoomOperationResultMapper;

@ExtendWith(MockitoExtension.class)
class RoomOperationsServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private AuditTrail auditTrail;

    @Test
    void shouldUpdateRoomStatusAndWriteAudit() {
        var service = service();
        var room = room(RoomStatus.CLEANING);

        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateRoomStatus(new UpdateRoomStatusCommand(1L, RoomStatus.AVAILABLE));

        assertEquals("AVAILABLE", result.status());

        var savedRoom = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(savedRoom.capture());
        assertEquals(RoomStatus.AVAILABLE, savedRoom.getValue().status());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.UPDATE_ROOM_STATUS),
                eq(AuditEntityType.ROOM),
                eq("1"),
                eq("Room status changed to AVAILABLE")
        );
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
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
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
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
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
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    private RoomOperationsService service() {
        return new RoomOperationsService(
                roomRepository,
                currentUserPort,
                auditTrail,
                new RoomOperationResultMapper()
        );
    }

    private static Room room(RoomStatus status) {
        return new Room(1L, 10L, "101", 20L, 2, status);
    }
}
