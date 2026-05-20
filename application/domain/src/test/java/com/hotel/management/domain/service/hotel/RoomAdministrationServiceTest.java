package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomFactory;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(MockitoExtension.class)
class RoomAdministrationServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AuditTrail auditTrail;

    @Test
    void shouldCreateRoomAndWriteAudit() {
        var service = service();
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 1001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomRepository.findById(1206L)).thenReturn(Optional.empty());
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createRoom(admin(), createRoomCommand(1206L, 1001L, 1102L, RoomStatus.AVAILABLE));

        assertEquals(1206L, result.roomId());
        assertEquals("401", result.roomNumber());
        assertEquals("AVAILABLE", result.status());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.CREATE_ROOM),
                eq(AuditEntityType.ROOM),
                eq("1206"),
                eq("Room created")
        );
    }

    @Test
    void shouldUpdateRoomAndWriteAudit() {
        var service = service();
        when(roomRepository.findById(1203L)).thenReturn(Optional.of(room(1203L, 1001L, 1102L, RoomStatus.AVAILABLE)));
        when(roomTypeRepository.findById(1103L)).thenReturn(Optional.of(roomType(1103L, 1001L, "Family")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateRoom(admin(), updateRoomCommand(1203L, 1001L, 1103L, RoomStatus.MAINTENANCE));

        assertEquals(1103L, result.roomTypeId());
        assertEquals("MAINTENANCE", result.status());

        verify(auditTrail).record(any(), eq(AuditActionType.UPDATE_ROOM), eq(AuditEntityType.ROOM), eq("1203"), eq("Room updated"));
    }

    @Test
    void shouldRejectRoomTypeFromDifferentHotel() {
        var service = service();
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 2001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));

        assertThrows(
                ValidationException.class,
                () -> service.createRoom(admin(), createRoomCommand(1206L, 1001L, 1102L, RoomStatus.AVAILABLE))
        );
        verify(roomRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectOccupiedRoomStatusInAdminFlow() {
        var service = service();
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 1001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomRepository.findById(1206L)).thenReturn(Optional.empty());

        assertThrows(
                ValidationException.class,
                () -> service.createRoom(admin(), createRoomCommand(1206L, 1001L, 1102L, RoomStatus.OCCUPIED))
        );
        verify(roomRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    private RoomAdministrationService service() {
        return new RoomAdministrationService(
                roomRepository,
                roomTypeRepository,
                hotelRepository,
                new RoomFactory(),
                auditTrail,
                new HotelQueryResultMapper()
        );
    }

    private static AuthenticatedUser admin() {
        return new AuthenticatedUser("admin-1", Set.of("ADMIN"));
    }

    private static Hotel hotel(Long hotelId, String name, HotelStatus status) {
        return new Hotel(
                hotelId,
                name,
                "Bratislava",
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                status,
                new HotelPolicy(true, true, 2, 12, 13)
        );
    }

    private static RoomType roomType(Long roomTypeId, Long hotelId, String name) {
        return new RoomType(
                roomTypeId,
                hotelId,
                name,
                new OccupancyPolicy(2, 1, 1, 3),
                new PetPolicy(false, 0, Set.of(), null, null),
                Money.of("150.00", "EUR"),
                "Room type",
                RoomTypeFeatures.empty()
        );
    }

    private static CreateRoomCommand createRoomCommand(Long roomId, Long hotelId, Long roomTypeId, RoomStatus status) {
        return new CreateRoomCommand(roomId, hotelId, "401", roomTypeId, 2, status);
    }

    private static UpdateRoomCommand updateRoomCommand(Long roomId, Long hotelId, Long roomTypeId, RoomStatus status) {
        return new UpdateRoomCommand(roomId, hotelId, "402", roomTypeId, 4, status);
    }

    private static Room room(Long roomId, Long hotelId, Long roomTypeId, RoomStatus status) {
        return new Room(roomId, hotelId, "401", roomTypeId, 2, status);
    }
}
