package com.hotel.management;

import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.service.guest.GuestFacade;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalGuestFacade implements GuestFacade {

    private final GuestFacade delegate;

    public TransactionalGuestFacade(GuestFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public Guest resolveGuest(AuthenticatedUser actor) {
        return delegate.resolveGuest(actor);
    }
}
