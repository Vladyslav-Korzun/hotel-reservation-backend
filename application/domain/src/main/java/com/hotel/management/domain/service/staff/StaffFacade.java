package com.hotel.management.domain.service.staff;

import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.staff.Staff;
import com.hotel.management.domain.staff.StaffResult;

import java.util.List;

public interface StaffFacade {

    List<StaffResult> listStaff(AuthenticatedUser actor);

    StaffResult assignStaffToHotel(AuthenticatedUser actor, AssignStaffToHotelCommand command);

    Staff resolveStaff(AuthenticatedUser actor);
}
