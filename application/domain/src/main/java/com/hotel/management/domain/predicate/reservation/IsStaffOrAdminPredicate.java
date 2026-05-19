package com.hotel.management.domain.predicate.reservation;

import com.hotel.management.domain.shared.security.AuthenticatedUser;

import java.util.function.Predicate;

public final class IsStaffOrAdminPredicate implements Predicate<AuthenticatedUser> {

    public static final IsStaffOrAdminPredicate INSTANCE = new IsStaffOrAdminPredicate();

    private IsStaffOrAdminPredicate() {
    }

    @Override
    public boolean test(AuthenticatedUser user) {
        return user != null && (user.isStaff() || user.isAdmin());
    }
}