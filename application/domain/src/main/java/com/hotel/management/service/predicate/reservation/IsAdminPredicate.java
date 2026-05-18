package com.hotel.management.service.predicate.reservation;

import com.hotel.management.service.security.AuthenticatedUser;

import java.util.function.Predicate;

public final class IsAdminPredicate implements Predicate<AuthenticatedUser> {

    public static final IsAdminPredicate INSTANCE = new IsAdminPredicate();

    private IsAdminPredicate() {
    }

    @Override
    public boolean test(AuthenticatedUser user) {
        return user != null && user.isAdmin();
    }
}
