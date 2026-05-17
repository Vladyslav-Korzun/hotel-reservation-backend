package com.hotel.management.domain.serviceoffering;

import com.hotel.management.domain.shared.value.Money;

public class ServiceOfferingFactory {

    public ServiceOffering create(
            Long serviceOfferingId,
            Long hotelId,
            String code,
            String name,
            String description,
            Money price,
            boolean active,
            String availabilityRule
    ) {
        return new ServiceOffering(serviceOfferingId, hotelId, code, name, description, price, active, availabilityRule);
    }
}
