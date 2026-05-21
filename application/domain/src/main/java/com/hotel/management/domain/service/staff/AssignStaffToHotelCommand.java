package com.hotel.management.domain.service.staff;

import com.hotel.management.domain.shared.exception.ValidationException;

public record AssignStaffToHotelCommand(Long staffId, Long hotelId) {

    public AssignStaffToHotelCommand {
        if (staffId == null) {
            throw new ValidationException("staffId is required");
        }
        if (hotelId == null) {
            throw new ValidationException("hotelId is required");
        }
    }
}
