package com.hotel.management;

import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.service.hotel.CreateServiceOfferingCommand;
import com.hotel.management.domain.service.hotel.DeactivateServiceOfferingCommand;
import com.hotel.management.domain.service.hotel.ServiceOfferingFacade;
import com.hotel.management.domain.service.hotel.UpdateServiceOfferingCommand;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalServiceOfferingFacade implements ServiceOfferingFacade {

    private final ServiceOfferingFacade delegate;

    public TransactionalServiceOfferingFacade(ServiceOfferingFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public HotelServiceOfferingResult createServiceOffering(AuthenticatedUser actor, CreateServiceOfferingCommand command) {
        return delegate.createServiceOffering(actor, command);
    }

    @Override
    @Transactional
    public HotelServiceOfferingResult updateServiceOffering(AuthenticatedUser actor, UpdateServiceOfferingCommand command) {
        return delegate.updateServiceOffering(actor, command);
    }

    @Override
    @Transactional
    public void deactivateServiceOffering(AuthenticatedUser actor, DeactivateServiceOfferingCommand command) {
        delegate.deactivateServiceOffering(actor, command);
    }
}
