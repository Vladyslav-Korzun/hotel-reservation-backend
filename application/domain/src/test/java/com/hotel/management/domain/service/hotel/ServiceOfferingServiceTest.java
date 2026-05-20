package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingFactory;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.value.Money;
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
class ServiceOfferingServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ServiceOfferingRepository serviceOfferingRepository;

    @Mock
    private AuditTrail auditTrail;

    @Test
    void shouldCreateServiceOfferingAndWriteAudit() {
        var service = service();
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L)).thenReturn(Optional.empty());
        when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createServiceOffering(admin(), createServiceOfferingCommand(1001L, 9001L));

        assertEquals(9001L, result.serviceOfferingId());
        assertEquals("BREAKFAST", result.code());
        assertEquals(true, result.active());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.CREATE_SERVICE_OFFERING),
                eq(AuditEntityType.SERVICE_OFFERING),
                eq("9001"),
                eq("Service offering created")
        );
    }

    @Test
    void shouldUpdateServiceOfferingAndWriteAudit() {
        var service = service();
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L))
                .thenReturn(Optional.of(serviceOffering(9001L, 1001L, "BREAKFAST", true)));
        when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.updateServiceOffering(admin(), updateServiceOfferingCommand(1001L, 9001L));

        assertEquals("SPA", result.code());
        assertEquals("Spa access", result.name());
        assertEquals(true, result.active());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.UPDATE_SERVICE_OFFERING),
                eq(AuditEntityType.SERVICE_OFFERING),
                eq("9001"),
                eq("Service offering updated")
        );
    }

    @Test
    void shouldDeactivateServiceOfferingAndWriteAudit() {
        var service = service();
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L))
                .thenReturn(Optional.of(serviceOffering(9001L, 1001L, "BREAKFAST", true)));
        when(serviceOfferingRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deactivateServiceOffering(admin(), new DeactivateServiceOfferingCommand(1001L, 9001L));

        var savedServiceOffering = ArgumentCaptor.forClass(ServiceOffering.class);
        verify(serviceOfferingRepository).save(savedServiceOffering.capture());
        assertEquals(false, savedServiceOffering.getValue().active());

        verify(auditTrail).record(
                any(),
                eq(AuditActionType.DEACTIVATE_SERVICE_OFFERING),
                eq(AuditEntityType.SERVICE_OFFERING),
                eq("9001"),
                eq("Service offering deactivated")
        );
    }

    @Test
    void shouldRejectServiceOfferingFromDifferentHotel() {
        var service = service();
        when(hotelRepository.findById(1001L)).thenReturn(Optional.of(hotel(1001L, "Hotel", HotelStatus.ACTIVE)));
        when(serviceOfferingRepository.findById(9001L))
                .thenReturn(Optional.of(serviceOffering(9001L, 2001L, "BREAKFAST", true)));

        assertThrows(
                ValidationException.class,
                () -> service.updateServiceOffering(admin(), updateServiceOfferingCommand(1001L, 9001L))
        );
        verify(serviceOfferingRepository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }

    private ServiceOfferingService service() {
        return new ServiceOfferingService(
                serviceOfferingRepository,
                hotelRepository,
                new ServiceOfferingFactory(),
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
