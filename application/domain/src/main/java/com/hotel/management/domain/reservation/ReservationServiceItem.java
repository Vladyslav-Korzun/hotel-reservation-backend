package com.hotel.management.domain.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;

public final class ReservationServiceItem {

    private final Long id;
    private final String reservationId;
    private final Long serviceOfferingId;
    private final String serviceNameSnapshot;
    private final Money priceSnapshot;
    private final int quantity;

    public ReservationServiceItem(
            Long id,
            String reservationId,
            Long serviceOfferingId,
            String serviceNameSnapshot,
            Money priceSnapshot,
            int quantity
    ) {
        this.id = id;
        this.reservationId = requireText(reservationId, "reservationId is required");
        this.serviceOfferingId = require(serviceOfferingId, "serviceOfferingId is required");
        this.serviceNameSnapshot = requireText(serviceNameSnapshot, "serviceNameSnapshot is required");
        this.priceSnapshot = require(priceSnapshot, "priceSnapshot is required");
        if (quantity <= 0) {
            throw new ValidationException("quantity must be greater than zero");
        }
        this.quantity = quantity;
    }

    public static ReservationServiceItem snapshot(
            String reservationId,
            ServiceOffering serviceOffering,
            int quantity
    ) {
        require(serviceOffering, "serviceOffering is required");
        if (!serviceOffering.active()) {
            throw new ValidationException("Inactive service offering cannot be selected");
        }
        return new ReservationServiceItem(
                null,
                reservationId,
                serviceOffering.id(),
                serviceOffering.name(),
                serviceOffering.price(),
                quantity
        );
    }

    public Money totalPrice() {
        return priceSnapshot.multiply(quantity);
    }

    public Long id() {
        return id;
    }

    public String reservationId() {
        return reservationId;
    }

    public Long serviceOfferingId() {
        return serviceOfferingId;
    }

    public String serviceNameSnapshot() {
        return serviceNameSnapshot;
    }

    public Money priceSnapshot() {
        return priceSnapshot;
    }

    public int quantity() {
        return quantity;
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
        return value;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
