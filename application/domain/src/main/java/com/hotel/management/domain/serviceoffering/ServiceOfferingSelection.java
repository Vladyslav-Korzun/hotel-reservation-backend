package com.hotel.management.domain.serviceoffering;

import com.hotel.management.domain.shared.exception.ValidationException;

public record ServiceOfferingSelection(Long serviceOfferingId, int quantity) {

    public ServiceOfferingSelection {
        if (serviceOfferingId == null) {
            throw new ValidationException("serviceOfferingId is required");
        }
        if (quantity <= 0) {
            throw new ValidationException("quantity must be greater than zero");
        }
    }
}
