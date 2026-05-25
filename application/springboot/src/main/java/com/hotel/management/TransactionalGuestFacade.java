package com.hotel.management;

import com.hotel.management.domain.service.guest.GuestFacade;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalGuestFacade implements GuestFacade {

    private final GuestFacade delegate;

    public TransactionalGuestFacade(GuestFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public Long findOrCreateByKeycloakId(String keycloakId, Long legacyGuestId,
                                          String email, String firstName, String lastName) {
        return delegate.findOrCreateByKeycloakId(keycloakId, legacyGuestId, email, firstName, lastName);
    }
}
