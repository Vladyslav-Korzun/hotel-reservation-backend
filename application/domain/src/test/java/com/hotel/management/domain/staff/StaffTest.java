package com.hotel.management.domain.staff;

import com.hotel.management.domain.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffTest {

    @Test
    void shouldCreateUnassignedStaff() {
        Staff staff = Staff.unassigned("sub-123");

        assertEquals("sub-123", staff.externalId());
        assertNull(staff.hotelId());
        assertFalse(staff.isAssigned());
    }

    @Test
    void shouldAssignStaffToHotel() {
        Staff assigned = Staff.unassigned("sub-123").assignToHotel(1L);

        assertEquals(1L, assigned.hotelId());
        assertTrue(assigned.isAssigned());
        assertEquals("sub-123", assigned.externalId());
    }

    @Test
    void shouldRejectBlankExternalId() {
        assertThrows(ValidationException.class, () -> new Staff(1L, " ", 1L));
    }

    @Test
    void shouldRejectAssigningNullHotel() {
        assertThrows(ValidationException.class, () -> Staff.unassigned("sub-123").assignToHotel(null));
    }
}
