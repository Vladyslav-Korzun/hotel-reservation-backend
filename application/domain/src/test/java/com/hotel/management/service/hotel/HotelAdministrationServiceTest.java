package com.hotel.management.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelFactory;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.room.RoomFactory;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeFactory;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingFactory;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.service.exception.ForbiddenException;
import com.hotel.management.domain.audit.AuditLogPort;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
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
class HotelAdministrationServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ServiceOfferingRepository serviceOfferingRepository;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private ClockPort clockPort;

    @Mock
    private AuditLogPort auditLogPort;

    @Test
    void shouldCreateHotelAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1004L)).thenReturn(Optional.empty());
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.createHotel(createCommand(1004L));

        assertEquals(1004L, result.hotelId());
        assertEquals("ACTIVE", result.status());

        var savedHotel = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepository).save(savedHotel.capture());
        assertEquals("New Hotel", savedHotel.getValue().name());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.CREATE_HOTEL, auditEntry.getValue().actionType());
        assertEquals(AuditEntityType.HOTEL, auditEntry.getValue().entityType());
        assertEquals("1004", auditEntry.getValue().entityId());
    }

    @Test
    void shouldUpdateHotelAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Old Hotel", HotelStatus.ACTIVE)));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.updateHotel(updateCommand(1001L));

        assertEquals("Updated Hotel", result.name());
        assertEquals("UNDER_MAINTENANCE", result.status());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.UPDATE_HOTEL, auditEntry.getValue().actionType());
        assertEquals("1001", auditEntry.getValue().entityId());
    }

    @Test
    void shouldRejectNonAdminUser() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-1", Set.of("STAFF")));

        assertThrows(ForbiddenException.class, () -> service.createHotel(createCommand(1004L)));
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateHotelId() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Existing Hotel", HotelStatus.ACTIVE)));

        assertThrows(ValidationException.class, () -> service.createHotel(createCommand(1001L)));
        verify(hotelRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    @Test
    void shouldRejectMissingHotelOnUpdate() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateHotel(updateCommand(9999L)));
        verify(hotelRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    @Test
    void shouldCreateRoomTypeAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomTypeRepository.findById(1104L)).thenReturn(Optional.empty());
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.createRoomType(createRoomTypeCommand(1104L, 1001L));

        assertEquals(1104L, result.roomTypeId());
        assertEquals(1001L, result.hotelId());
        assertEquals("Suite", result.name());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.CREATE_ROOM_TYPE, auditEntry.getValue().actionType());
        assertEquals(AuditEntityType.ROOM_TYPE, auditEntry.getValue().entityType());
        assertEquals("1104", auditEntry.getValue().entityId());
    }

    @Test
    void shouldUpdateRoomTypeAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 1001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.updateRoomType(updateRoomTypeCommand(1102L));

        assertEquals("Updated Suite", result.name());
        assertEquals(3, result.maxAdults());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.UPDATE_ROOM_TYPE, auditEntry.getValue().actionType());
        assertEquals("1102", auditEntry.getValue().entityId());
    }

    @Test
    void shouldCreateRoomAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 1001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomRepository.findById(1206L)).thenReturn(Optional.empty());
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.createRoom(createRoomCommand(1206L, 1001L, 1102L, RoomStatus.AVAILABLE));

        assertEquals(1206L, result.roomId());
        assertEquals("401", result.roomNumber());
        assertEquals("AVAILABLE", result.status());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.CREATE_ROOM, auditEntry.getValue().actionType());
        assertEquals(AuditEntityType.ROOM, auditEntry.getValue().entityType());
        assertEquals("1206", auditEntry.getValue().entityId());
    }

    @Test
    void shouldUpdateRoomAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(roomRepository.findById(1203L)).thenReturn(Optional.of(room(1203L, 1001L, 1102L, RoomStatus.AVAILABLE)));
        when(roomTypeRepository.findById(1103L)).thenReturn(Optional.of(roomType(1103L, 1001L, "Family")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.updateRoom(updateRoomCommand(1203L, 1001L, 1103L, RoomStatus.MAINTENANCE));

        assertEquals(1103L, result.roomTypeId());
        assertEquals("MAINTENANCE", result.status());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.UPDATE_ROOM, auditEntry.getValue().actionType());
        assertEquals("1203", auditEntry.getValue().entityId());
    }

    @Test
    void shouldRejectRoomTypeFromDifferentHotel() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 2001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));

        assertThrows(
                ValidationException.class,
                () -> service.createRoom(createRoomCommand(1206L, 1001L, 1102L, RoomStatus.AVAILABLE))
        );
        verify(roomRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    @Test
    void shouldRejectOccupiedRoomStatusInAdminFlow() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 1001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomRepository.findById(1206L)).thenReturn(Optional.empty());

        assertThrows(
                ValidationException.class,
                () -> service.createRoom(createRoomCommand(1206L, 1001L, 1102L, RoomStatus.OCCUPIED))
        );
        verify(roomRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    @Test
    void shouldCreateServiceOfferingAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L)).thenReturn(Optional.empty());
        when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.createServiceOffering(createServiceOfferingCommand(1001L, 9001L));

        assertEquals(9001L, result.serviceOfferingId());
        assertEquals("BREAKFAST", result.code());
        assertEquals(true, result.active());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.CREATE_SERVICE_OFFERING, auditEntry.getValue().actionType());
        assertEquals(AuditEntityType.SERVICE_OFFERING, auditEntry.getValue().entityType());
        assertEquals("9001", auditEntry.getValue().entityId());
    }

    @Test
    void shouldUpdateServiceOfferingAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L))
                .thenReturn(Optional.of(serviceOffering(9001L, 1001L, "BREAKFAST", true)));
        when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        var result = service.updateServiceOffering(updateServiceOfferingCommand(1001L, 9001L));

        assertEquals("SPA", result.code());
        assertEquals("Spa access", result.name());
        assertEquals(true, result.active());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.UPDATE_SERVICE_OFFERING, auditEntry.getValue().actionType());
        assertEquals("9001", auditEntry.getValue().entityId());
    }

    @Test
    void shouldDeactivateServiceOfferingAndWriteAudit() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L))
                .thenReturn(Optional.of(serviceOffering(9001L, 1001L, "BREAKFAST", true)));
        when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockPort.now()).thenReturn(Instant.parse("2026-05-10T10:00:00Z"));

        service.deactivateServiceOffering(new DeactivateServiceOfferingCommand(1001L, 9001L));

        var savedServiceOffering = ArgumentCaptor.forClass(ServiceOffering.class);
        verify(serviceOfferingRepository).save(savedServiceOffering.capture());
        assertEquals(false, savedServiceOffering.getValue().active());

        var auditEntry = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogPort).append(auditEntry.capture());
        assertEquals(AuditActionType.DEACTIVATE_SERVICE_OFFERING, auditEntry.getValue().actionType());
        assertEquals("9001", auditEntry.getValue().entityId());
    }

    @Test
    void shouldRejectServiceOfferingFromDifferentHotel() {
        var service = service();
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("admin-1", Set.of("ADMIN")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L))
                .thenReturn(Optional.of(serviceOffering(9001L, 2001L, "BREAKFAST", true)));

        assertThrows(
                ValidationException.class,
                () -> service.updateServiceOffering(updateServiceOfferingCommand(1001L, 9001L))
        );
        verify(serviceOfferingRepository, never()).save(any());
        verify(auditLogPort, never()).append(any());
    }

    private HotelAdministrationService service() {
        return new HotelAdministrationService(
                hotelRepository,
                roomTypeRepository,
                roomRepository,
                serviceOfferingRepository,
                currentUserPort,
                clockPort,
                auditLogPort,
                new HotelFactory(),
                new RoomTypeFactory(),
                new RoomFactory(),
                new ServiceOfferingFactory(),
                new HotelQueryResultMapper()
        );
    }

    private static CreateHotelCommand createCommand(Long hotelId) {
        return new CreateHotelCommand(
                hotelId,
                "New Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                HotelStatus.ACTIVE,
                true,
                true,
                2,
                12,
                13
        );
    }

    private static UpdateHotelCommand updateCommand(Long hotelId) {
        return new UpdateHotelCommand(
                hotelId,
                "Updated Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 9",
                5,
                "Updated city hotel",
                HotelStatus.UNDER_MAINTENANCE,
                true,
                false,
                2,
                12,
                13
        );
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

    private static CreateRoomTypeCommand createRoomTypeCommand(Long roomTypeId, Long hotelId) {
        return new CreateRoomTypeCommand(
                roomTypeId,
                hotelId,
                "Suite",
                2,
                2,
                1,
                4,
                true,
                1,
                Set.of(PetType.DOG),
                new java.math.BigDecimal("15.00"),
                new java.math.BigDecimal("20.00"),
                "EUR",
                new java.math.BigDecimal("220.00"),
                "EUR",
                "Suite room",
                "1 king bed + sofa bed",
                new java.math.BigDecimal("42.0"),
                Set.of(RoomAmenity.WIFI, RoomAmenity.PRIVATE_BATHROOM)
        );
    }

    private static UpdateRoomTypeCommand updateRoomTypeCommand(Long roomTypeId) {
        return new UpdateRoomTypeCommand(
                roomTypeId,
                "Updated Suite",
                3,
                2,
                1,
                5,
                false,
                0,
                Set.of(),
                null,
                null,
                null,
                new java.math.BigDecimal("250.00"),
                "EUR",
                "Updated suite room",
                "2 queen beds",
                new java.math.BigDecimal("48.0"),
                Set.of(RoomAmenity.WIFI, RoomAmenity.CITY_VIEW)
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

    private static CreateServiceOfferingCommand createServiceOfferingCommand(Long hotelId, Long serviceOfferingId) {
        return new CreateServiceOfferingCommand(
                hotelId,
                serviceOfferingId,
                "BREAKFAST",
                "Breakfast",
                "Breakfast buffet",
                new java.math.BigDecimal("12.00"),
                "EUR",
                true,
                "DAILY"
        );
    }

    private static UpdateServiceOfferingCommand updateServiceOfferingCommand(Long hotelId, Long serviceOfferingId) {
        return new UpdateServiceOfferingCommand(
                hotelId,
                serviceOfferingId,
                "SPA",
                "Spa access",
                "Wellness and spa access",
                new java.math.BigDecimal("40.00"),
                "EUR",
                true,
                "DAILY"
        );
    }

    private static ServiceOffering serviceOffering(Long serviceOfferingId, Long hotelId, String code, boolean active) {
        return new ServiceOffering(
                serviceOfferingId,
                hotelId,
                code,
                "Breakfast",
                "Breakfast buffet",
                Money.of("12.00", "EUR"),
                active,
                "DAILY"
        );
    }
}