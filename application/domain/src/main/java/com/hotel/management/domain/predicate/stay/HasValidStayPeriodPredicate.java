package com.hotel.management.domain.predicate.stay;

import com.hotel.management.domain.shared.value.StayPeriod;

import java.util.function.Predicate;

public final class HasValidStayPeriodPredicate implements Predicate<StayPeriod> {

    public static final HasValidStayPeriodPredicate INSTANCE = new HasValidStayPeriodPredicate();

    private HasValidStayPeriodPredicate() {
    }

    @Override
    public boolean test(StayPeriod stayPeriod) {
        return stayPeriod != null && stayPeriod.checkOut().isAfter(stayPeriod.checkIn());
    }
}
