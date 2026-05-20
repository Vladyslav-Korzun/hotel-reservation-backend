package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelFactory;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
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

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private AuditTrail auditTrail;

    @Test
    void shouldCreateHotelAndWriteAudit() {
        var service = service();
        when(hotelRepository.findById(1004L)).thenReturn(Optional.empty());
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createHotel(admin(), createCommand(1004L));

        assertEquals(1004L, result.hotelId());
        assertEquals("ACTIVE", result.status());

        var savedHotel = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepository).save(savedHotel.capture());
        assertEquals("New Hotel", savedHotel.getValue().name());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.CREATE_HOTEL),
                eq(AuditEntityType.HOTEL),
                eq("1004"),
                eq("Hotel created")
        );
    }

    @Test
    void shouldUpdateHotelAndWriteAudit() {
        var service = service();
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Old Hotel", HotelStatus.ACTIVE)));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateHotel(admin(), updateCommand(1001L));

        assertEquals("Updated Hotel", result.name());
        assertEquals("UNDER_MAINTENANCE", result.status());

        verify(auditTrail).record(any(), eq(AuditActionType.UPDATE_HOTEL), eq(AuditEntityType.HOTEL), eq("1001"), eq("Hotel updated"));
    }

    @Test
    void shouldRejectNonAdminUser() {
        var service = service();

        assertThrows(ForbiddenException.class, () -> service.createHotel(new AuthenticatedUser("staff-1", Set.of("STAFF")), createCommand(1004L)));
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateHotelId() {
        var service = service();
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Existing Hotel", HotelStatus.ACTIVE)));

        assertThrows(ValidationException.class, () -> service.createHotel(admin(), createCommand(1001L)));
        verify(hotelRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectMissingHotelOnUpdate() {
        var service = service();
        when(hotelRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateHotel(admin(), updateCommand(9999L)));
        verify(hotelRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    private HotelService service() {
        return new HotelService(
                hotelRepository,
                new HotelFactory(),
                auditTrail,
                new HotelQueryResultMapper()
        );
    }

    private static AuthenticatedUser admin() {
        return new AuthenticatedUser("admin-1", Set.of("ADMIN"));
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
}
