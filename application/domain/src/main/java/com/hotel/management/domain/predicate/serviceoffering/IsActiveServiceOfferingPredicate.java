package com.hotel.management.domain.predicate.serviceoffering;

import com.hotel.management.domain.serviceoffering.ServiceOffering;

import java.util.function.Predicate;

public final class IsActiveServiceOfferingPredicate implements Predicate<ServiceOffering> {

    public static final IsActiveServiceOfferingPredicate INSTANCE = new IsActiveServiceOfferingPredicate();

    private IsActiveServiceOfferingPredicate() {
    }

    @Override
    public boolean test(ServiceOffering serviceOffering) {
        return serviceOffering != null && serviceOffering.active();
    }
}
