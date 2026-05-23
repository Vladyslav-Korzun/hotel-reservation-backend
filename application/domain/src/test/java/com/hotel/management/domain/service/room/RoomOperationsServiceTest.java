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
import com.hotel.management.domain.service.staff.AssignStaffToHotelCommand;
import com.hotel.management.domain.service.staff.HotelScopePolicy;
import com.hotel.management.domain.service.staff.StaffFacade;
import com.hotel.management.domain.staff.Staff;
import com.hotel.management.domain.staff.StaffResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private AuditTrail auditTrail;

    @Test
    void shouldUpdateRoomStatusAndWriteAudit() {
        var service = service();
        var room = room(RoomStatus.CLEANING);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateRoomStatus(staff(), new UpdateRoomStatusCommand(1L, RoomStatus.AVAILABLE));

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

        assertThrows(
                ForbiddenException.class,
                () -> service.updateRoomStatus(new AuthenticatedUser("guest-1", Set.of("GUEST")), new UpdateRoomStatusCommand(1L, RoomStatus.CLEANING))
        );
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void shouldRejectMissingRoomId() {
        var service = service();

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(staff(), new UpdateRoomStatusCommand(null, RoomStatus.CLEANING))
        );
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void shouldRejectMissingStatus() {
        var service = service();

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(staff(), new UpdateRoomStatusCommand(1L, null))
        );
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void shouldRejectUnknownRoom() {
        var service = service();
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.updateRoomStatus(staff(), new UpdateRoomStatusCommand(1L, RoomStatus.CLEANING))
        );
        verify(roomRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectManualOccupiedStatus() {
        var service = service();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(RoomStatus.AVAILABLE)));

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(staff(), new UpdateRoomStatusCommand(1L, RoomStatus.OCCUPIED))
        );
        verify(roomRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectOccupiedRoomMovedToMaintenance() {
        var service = service();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(RoomStatus.OCCUPIED)));

        assertThrows(
                ValidationException.class,
                () -> service.updateRoomStatus(staff(), new UpdateRoomStatusCommand(1L, RoomStatus.MAINTENANCE))
        );
        verify(roomRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void shouldListRoomsForStaffHotel() {
        var service = service();
        when(roomRepository.findByHotelIds(List.of(10L)))
                .thenReturn(List.of(room(RoomStatus.AVAILABLE), room(RoomStatus.OCCUPIED)));

        var result = service.listRooms(staff(), null);

        assertEquals(2, result.size());
    }

    @Test
    void shouldListRoomsForAdminByHotelId() {
        var service = service();
        when(roomRepository.findByHotelIds(List.of(7L))).thenReturn(List.of(room(RoomStatus.AVAILABLE)));

        var result = service.listRooms(admin(), 7L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldRejectAdminListRoomsWithoutHotelId() {
        var service = service();

        assertThrows(ValidationException.class, () -> service.listRooms(admin(), null));
        verify(roomRepository, never()).findByHotelIds(any());
    }

    private RoomOperationsService service() {
        return new RoomOperationsService(
                roomRepository,
                auditTrail,
                new RoomOperationResultMapper(),
                new HotelScopePolicy(staffAssignedToHotel(10L))
        );
    }

    private static AuthenticatedUser staff() {
        return new AuthenticatedUser("staff-1", Set.of("STAFF"));
    }

    private static AuthenticatedUser admin() {
        return new AuthenticatedUser("admin-1", Set.of("ADMIN"));
    }

    private static Room room(RoomStatus status) {
        return new Room(1L, 10L, "101", 20L, 2, status);
    }

    private static StaffFacade staffAssignedToHotel(Long hotelId) {
        return new StaffFacade() {
            @Override
            public Staff resolveStaff(AuthenticatedUser actor) {
                return new Staff(1L, "staff-sub", hotelId);
            }

            @Override
            public List<StaffResult> listStaff(AuthenticatedUser actor) {
                throw new UnsupportedOperationException();
            }

            @Override
            public StaffResult assignStaffToHotel(AuthenticatedUser actor, AssignStaffToHotelCommand command) {
                throw new UnsupportedOperationException();
            }

            @Override
            public StaffResult unassignStaffFromHotel(AuthenticatedUser actor, Long staffId) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
