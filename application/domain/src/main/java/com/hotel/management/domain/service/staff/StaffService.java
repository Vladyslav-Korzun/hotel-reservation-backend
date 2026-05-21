package com.hotel.management.domain.service.staff;

import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.staff.Staff;
import com.hotel.management.domain.staff.StaffRepository;
import com.hotel.management.domain.staff.StaffResult;

import java.util.List;

public class StaffService implements StaffFacade {

    private final StaffRepository staffRepository;
    private final HotelRepository hotelRepository;

    public StaffService(StaffRepository staffRepository, HotelRepository hotelRepository) {
        this.staffRepository = staffRepository;
        this.hotelRepository = hotelRepository;
    }

    @Override
    public List<StaffResult> listStaff(AuthenticatedUser actor) {
        requireActor(actor).requireAdmin();
        return staffRepository.findAll().stream()
                .map(StaffService::toResult)
                .toList();
    }

    @Override
    public StaffResult assignStaffToHotel(AuthenticatedUser actor, AssignStaffToHotelCommand command) {
        requireActor(actor).requireAdmin();
        if (command == null) {
            throw new ValidationException("staff assignment command is required");
        }
        Staff staff = staffRepository.findById(command.staffId())
                .orElseThrow(() -> new NotFoundException("Staff not found: " + command.staffId()));
        hotelRepository.findById(command.hotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + command.hotelId()));
        Staff assigned = staffRepository.save(staff.assignToHotel(command.hotelId()));
        return toResult(assigned);
    }

    @Override
    public Staff resolveStaff(AuthenticatedUser actor) {
        requireActor(actor).requireStaff();
        String subject = actor.subject();
        if (subject == null || subject.isBlank()) {
            throw new ForbiddenException("staff subject claim is required");
        }
        return staffRepository.findByExternalId(subject)
                .orElseGet(() -> staffRepository.save(Staff.unassigned(subject)));
    }

    private static AuthenticatedUser requireActor(AuthenticatedUser actor) {
        if (actor == null) {
            throw new ForbiddenException("authentication is required");
        }
        return actor;
    }

    private static StaffResult toResult(Staff staff) {
        return new StaffResult(staff.id(), staff.externalId(), staff.hotelId());
    }
}
