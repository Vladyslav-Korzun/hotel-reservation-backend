package com.hotel.management.service.predicate.reservation;

import com.hotel.management.service.security.AuthenticatedUser;

import java.util.function.Predicate;

public final class IsGuestPredicate implements Predicate<AuthenticatedUser> {

    public static final IsGuestPredicate INSTANCE = new IsGuestPredicate();

    private IsGuestPredicate() {
    }

    @Override
    public boolean test(AuthenticatedUser user) {
        return user != null && user.isGuest();
    }
}
