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

    public static Money of(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            throw new ValidationException("amount is required");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new ValidationException("currency is required");
        }
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public Money plus(Money other) {
        if (other == null) {
            throw new ValidationException("money is required");
        }
        if (!currency.equals(other.currency)) {
            throw new ValidationException("currency must match");
        }
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        if (other == null) {
            throw new ValidationException("money is required");
        }
        if (!currency.equals(other.currency)) {
            throw new ValidationException("currency must match");
        }
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new ValidationException("multiplier must not be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(multiplier)), currency);
    }

    public static Money zero(String currencyCode) {
        return Money.of("0.00", currencyCode);
    }
}
