package com.hotel.management.domain.shared.value;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null) {
            throw new ValidationException("amount is required");
        }
        if (currency == null) {
            throw new ValidationException("currency is required");
        }
        if (amount.signum() < 0) {
            throw new ValidationException("amount must not be negative");
        }
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }
}
