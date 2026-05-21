package com.hotel.management.domain.service.staff;

import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.staff.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelScopePolicyTest {

    @Mock
    private StaffFacade staffFacade;

    private HotelScopePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new HotelScopePolicy(staffFacade);
    }

    private static AuthenticatedUser admin() {
        return new AuthenticatedUser("admin-1", Set.of("ADMIN"), null, "admin-sub");
    }

    private static AuthenticatedUser staff() {
        return new AuthenticatedUser("staff-1", Set.of("STAFF"), null, "staff-sub");
    }

    @Test
    void shouldLeaveAdminUnrestricted() {
        assertNull(policy.resolveAccessibleHotel(admin()));
        assertDoesNotThrow(() -> policy.assertCanAccessHotel(admin(), 1L));
    }

    @Test
    void shouldAllowStaffForAssignedHotel() {
        when(staffFacade.resolveStaff(any())).thenReturn(new Staff(1L, "staff-sub", 5L));

        assertEquals(5L, policy.resolveAccessibleHotel(staff()));
        assertDoesNotThrow(() -> policy.assertCanAccessHotel(staff(), 5L));
    }

    @Test
    void shouldRejectStaffForAnotherHotel() {
        when(staffFacade.resolveStaff(any())).thenReturn(new Staff(1L, "staff-sub", 5L));

        assertThrows(ForbiddenException.class, () -> policy.assertCanAccessHotel(staff(), 9L));
    }

    @Test
    void shouldRejectUnassignedStaff() {
        when(staffFacade.resolveStaff(any())).thenReturn(Staff.unassigned("staff-sub"));

        assertThrows(ForbiddenException.class, () -> policy.resolveAccessibleHotel(staff()));
    }
}
