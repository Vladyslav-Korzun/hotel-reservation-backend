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
import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFactory;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private AuditTrail auditTrail;

    @Test
    void shouldCreateRoomTypeAndWriteAudit() {
        var service = service();
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomTypeRepository.findById(1104L)).thenReturn(Optional.empty());
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createRoomType(admin(), createRoomTypeCommand(1104L, 1001L));

        assertEquals(1104L, result.roomTypeId());
        assertEquals(1001L, result.hotelId());
        assertEquals("Suite", result.name());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.CREATE_ROOM_TYPE),
                eq(AuditEntityType.ROOM_TYPE),
                eq("1104"),
                eq("Room type created")
        );
    }

    @Test
    void shouldUpdateRoomTypeAndWriteAudit() {
        var service = service();
        when(roomTypeRepository.findById(1102L)).thenReturn(Optional.of(roomType(1102L, 1001L, "Double")));
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(roomTypeRepository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateRoomType(admin(), updateRoomTypeCommand(1102L));

        assertEquals("Updated Suite", result.name());
        assertEquals(3, result.maxAdults());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.UPDATE_ROOM_TYPE),
                eq(AuditEntityType.ROOM_TYPE),
                eq("1102"),
                eq("Room type updated")
        );
    }

    private RoomTypeService service() {
        return new RoomTypeService(
                roomTypeRepository,
                hotelRepository,
                new RoomTypeFactory(),
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
}
