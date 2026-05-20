package com.hotel.management;

import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.service.hotel.CreateHotelCommand;
import com.hotel.management.domain.service.hotel.HotelFacade;
import com.hotel.management.domain.service.hotel.UpdateHotelCommand;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalHotelFacade implements HotelFacade {

    private final HotelFacade delegate;

    public TransactionalHotelFacade(HotelFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public HotelResult createHotel(AuthenticatedUser actor, CreateHotelCommand command) {
        return delegate.createHotel(actor, command);
    }

    @Override
    @Transactional
    public HotelResult updateHotel(AuthenticatedUser actor, UpdateHotelCommand command) {
        return delegate.updateHotel(actor, command);
    }
}
