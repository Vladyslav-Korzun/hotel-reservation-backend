package com.hotel.reservation.shared.port;

import com.hotel.reservation.shared.security.AuthenticatedUser;

public interface CurrentUserPort {

    AuthenticatedUser getCurrentUser();
}
