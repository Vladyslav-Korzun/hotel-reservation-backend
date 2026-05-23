package com.hotel.management;

import com.hotel.management.domain.service.staff.AssignStaffToHotelCommand;
import com.hotel.management.domain.service.staff.StaffFacade;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.staff.Staff;
import com.hotel.management.domain.staff.StaffResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class TransactionalStaffFacade implements StaffFacade {

    private final StaffFacade delegate;

    public TransactionalStaffFacade(StaffFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public List<StaffResult> listStaff(AuthenticatedUser actor) {
        return delegate.listStaff(actor);
    }

    @Override
    @Transactional
    public StaffResult assignStaffToHotel(AuthenticatedUser actor, AssignStaffToHotelCommand command) {
        return delegate.assignStaffToHotel(actor, command);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Staff resolveStaff(AuthenticatedUser actor) {
        return delegate.resolveStaff(actor);
    }
}
