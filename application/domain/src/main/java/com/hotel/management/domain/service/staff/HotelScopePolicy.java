package com.hotel.management.domain.service.staff;

import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.staff.Staff;

public class HotelScopePolicy {

    private final StaffFacade staffFacade;

    public HotelScopePolicy(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    /**
     * Returns the only hotel the actor may operate on, or {@code null} when the actor
     * is an admin and therefore not restricted to a single hotel.
     */
    public Long resolveAccessibleHotel(AuthenticatedUser actor) {
        if (actor != null && actor.isAdmin()) {
            return null;
        }
        Staff staff = staffFacade.resolveStaff(actor);
        if (!staff.isAssigned()) {
            throw new ForbiddenException("Staff member is not assigned to a hotel");
        }
        return staff.hotelId();
    }

    public void assertCanAccessHotel(AuthenticatedUser actor, Long targetHotelId) {
        Long accessibleHotelId = resolveAccessibleHotel(actor);
        if (accessibleHotelId == null) {
            return;
        }
        if (!accessibleHotelId.equals(targetHotelId)) {
            throw new ForbiddenException("Staff member can only access their assigned hotel");
        }
    }
}
