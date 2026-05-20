package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface HotelFacade {

    HotelResult createHotel(AuthenticatedUser actor, CreateHotelCommand command);

    HotelResult updateHotel(AuthenticatedUser actor, UpdateHotelCommand command);
}
