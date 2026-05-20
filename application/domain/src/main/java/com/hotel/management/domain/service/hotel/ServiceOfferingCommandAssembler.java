package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;

import java.math.BigDecimal;

final class ServiceOfferingCommandAssembler {

    private ServiceOfferingCommandAssembler() {
    }

    static Money toPrice(CreateServiceOfferingCommand command) {
        return toMoney(command.priceAmount(), command.priceCurrency(), "serviceOfferingPrice");
    }

    static Money toPrice(UpdateServiceOfferingCommand command) {
        return toMoney(command.priceAmount(), command.priceCurrency(), "serviceOfferingPrice");
    }

    private static Money toMoney(BigDecimal amount, String currencyCode, String fieldName) {
        if (amount == null) {
            throw new ValidationException(fieldName + " amount is required");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new ValidationException(fieldName + " currency is required");
        }
        return Money.of(amount, currencyCode);
    }
}
