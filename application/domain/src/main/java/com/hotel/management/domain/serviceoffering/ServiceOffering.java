package com.hotel.management.domain.serviceoffering;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;

public final class ServiceOffering {

    private final Long id;
    private final Long hotelId;
    private final String code;
    private final String name;
    private final String description;
    private final Money price;
    private final boolean active;
    private final String availabilityRule;

    public ServiceOffering(
            Long id,
            Long hotelId,
            String code,
            String name,
            String description,
            Money price,
            boolean active,
            String availabilityRule
    ) {
        this.id = require(id, "serviceOfferingId is required");
        this.hotelId = require(hotelId, "hotelId is required");
        this.code = requireText(code, "serviceOfferingCode is required").toUpperCase();
        this.name = requireText(name, "serviceOfferingName is required");
        this.description = description;
        this.price = require(price, "price is required");
        this.active = active;
        this.availabilityRule = availabilityRule;
    }

    public boolean belongsToHotel(Long hotelId) {
        return this.hotelId.equals(require(hotelId, "hotelId is required"));
    }

    public ServiceOffering activate() {
        return withActive(true);
    }

    public ServiceOffering deactivate() {
        return withActive(false);
    }

    public ServiceOffering updateDetails(
            String code,
            String name,
            String description,
            Money price,
            boolean active,
            String availabilityRule
    ) {
        return new ServiceOffering(id, hotelId, code, name, description, price, active, availabilityRule);
    }

    public Long id() {
        return id;
    }

    public Long hotelId() {
        return hotelId;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return price;
    }

    public boolean active() {
        return active;
    }

    public String availabilityRule() {
        return availabilityRule;
    }

    private ServiceOffering withActive(boolean active) {
        return new ServiceOffering(id, hotelId, code, name, description, price, active, availabilityRule);
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
