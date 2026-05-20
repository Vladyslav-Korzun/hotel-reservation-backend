package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface ServiceOfferingFacade {

    HotelServiceOfferingResult createServiceOffering(AuthenticatedUser actor, CreateServiceOfferingCommand command);

    HotelServiceOfferingResult updateServiceOffering(AuthenticatedUser actor, UpdateServiceOfferingCommand command);

    void deactivateServiceOffering(AuthenticatedUser actor, DeactivateServiceOfferingCommand command);
}
