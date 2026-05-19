package com.hotel.management.domain.shared.security;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

public interface CurrentUserPort {

    AuthenticatedUser getCurrentUser();
}