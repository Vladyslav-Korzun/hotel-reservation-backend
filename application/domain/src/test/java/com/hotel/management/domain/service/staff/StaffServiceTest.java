package com.hotel.management.domain.service.staff;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.staff.Staff;
import com.hotel.management.domain.staff.StaffRepository;
import com.hotel.management.domain.staff.StaffResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private HotelRepository hotelRepository;

    private StaffService staffService;

    @BeforeEach
    void setUp() {
        staffService = new StaffService(staffRepository, hotelRepository);
    }

    private static AuthenticatedUser admin() {
        return new AuthenticatedUser("admin-1", Set.of("ADMIN"), null, "admin-sub");
    }

    private static AuthenticatedUser staff(String subject) {
        return new AuthenticatedUser("staff-1", Set.of("STAFF"), null, subject);
    }

    @Test
    void shouldListStaffForAdmin() {
        when(staffRepository.findAll()).thenReturn(List.of(new Staff(1L, "sub-1", 5L)));

        List<StaffResult> result = staffService.listStaff(admin());

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().hotelId());
    }

    @Test
    void shouldRejectListStaffForNonAdmin() {
        assertThrows(ForbiddenException.class, () -> staffService.listStaff(staff("sub-1")));
    }

    @Test
    void shouldAssignStaffToHotel() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(new Staff(1L, "sub-1", null)));
        when(hotelRepository.findById(5L)).thenReturn(Optional.of(hotel()));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffResult result = staffService.assignStaffToHotel(admin(), new AssignStaffToHotelCommand(1L, 5L));

        assertEquals(5L, result.hotelId());
    }

    @Test
    void shouldRejectAssignWhenStaffNotFound() {
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> staffService.assignStaffToHotel(admin(), new AssignStaffToHotelCommand(1L, 5L))
        );
    }

    @Test
    void shouldRejectAssignWhenHotelNotFound() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(new Staff(1L, "sub-1", null)));
        when(hotelRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> staffService.assignStaffToHotel(admin(), new AssignStaffToHotelCommand(1L, 5L))
        );
    }

    @Test
    void shouldUnassignStaffFromHotel() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(new Staff(1L, "sub-1", 5L)));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffResult result = staffService.unassignStaffFromHotel(admin(), 1L);

        assertEquals(1L, result.id());
        assertEquals(null, result.hotelId());
    }

    @Test
    void shouldRejectUnassignForNonAdmin() {
        assertThrows(
                ForbiddenException.class,
                () -> staffService.unassignStaffFromHotel(staff("sub-1"), 1L)
        );
    }

    @Test
    void shouldRejectUnassignWhenStaffNotFound() {
        when(staffRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> staffService.unassignStaffFromHotel(admin(), 1L)
        );
    }

    @Test
    void shouldProvisionStaffOnFirstResolve() {
        when(staffRepository.findByExternalId("sub-new")).thenReturn(Optional.empty());
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Staff resolved = staffService.resolveStaff(staff("sub-new"));

        assertEquals("sub-new", resolved.externalId());
        assertFalse(resolved.isAssigned());
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    void shouldReturnExistingStaffOnResolveWhenDisplayNameUnchanged() {
        // Existing record already has the same username/email as the JWT — no save expected.
        var existing = new Staff(1L, "sub-1", 5L, "staff-1", null);
        when(staffRepository.findByExternalId("sub-1")).thenReturn(Optional.of(existing));

        Staff resolved = staffService.resolveStaff(staff("sub-1"));

        assertEquals(5L, resolved.hotelId());
        verify(staffRepository, never()).save(any());
    }

    @Test
    void shouldUpdateDisplayNameOnResolveWhenChanged() {
        // Existing record has a stale username — should be updated on login.
        var stale = new Staff(1L, "sub-1", 5L, "old-name", null);
        when(staffRepository.findByExternalId("sub-1")).thenReturn(Optional.of(stale));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Staff resolved = staffService.resolveStaff(staff("sub-1"));

        assertEquals("staff-1", resolved.username());
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    void shouldRejectResolveWithoutSubject() {
        assertThrows(
                ForbiddenException.class,
                () -> staffService.resolveStaff(new AuthenticatedUser("staff-1", Set.of("STAFF")))
        );
    }

    private static Hotel hotel() {
        return new Hotel(
                5L,
                "Danube Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                HotelStatus.ACTIVE,
                new HotelPolicy(true, true, 2, 12, 13)
        );
    }
}
