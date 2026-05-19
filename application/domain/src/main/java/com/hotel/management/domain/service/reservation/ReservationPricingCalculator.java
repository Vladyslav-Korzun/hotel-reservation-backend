package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.reservation.ReservationPriceSnapshot;
import com.hotel.management.domain.reservation.ReservationServiceItem;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.StayPeriod;

import java.util.List;

public class ReservationPricingCalculator {

    public ReservationPriceSnapshot calculate(
            RoomType roomType,
            StayPeriod stayPeriod,
            List<ReservationServiceItem> serviceItems
    ) {
        Money subtotal = calculateSubtotal(roomType, stayPeriod, serviceItems);
        Money discountAmount = Money.zero(subtotal.currency().getCurrencyCode());
        return calculate(roomType, stayPeriod, serviceItems, discountAmount);
    }

    public ReservationPriceSnapshot calculate(
            RoomType roomType,
            StayPeriod stayPeriod,
            List<ReservationServiceItem> serviceItems,
            Money discountAmount
    ) {
        if (roomType == null) {
            throw new ValidationException("roomType is required");
        }
        if (stayPeriod == null) {
            throw new ValidationException("stayPeriod is required");
        }
        List<ReservationServiceItem> items = serviceItems == null ? List.of() : serviceItems;

        Money basePrice = roomType.basePrice().multiply(stayPeriod.nights());
        Money servicesPrice = items.stream()
                .map(ReservationServiceItem::totalPrice)
                .reduce(Money.zero(basePrice.currency().getCurrencyCode()), Money::plus);
        Money subtotal = basePrice.plus(servicesPrice);
        Money normalizedDiscount = normalizeDiscount(discountAmount, subtotal);
        Money finalPrice = subtotal.minus(normalizedDiscount);

        return new ReservationPriceSnapshot(basePrice, servicesPrice, normalizedDiscount, finalPrice);
    }

    public Money calculateSubtotal(
            RoomType roomType,
            StayPeriod stayPeriod,
            List<ReservationServiceItem> serviceItems
    ) {
        if (roomType == null) {
            throw new ValidationException("roomType is required");
        }
        if (stayPeriod == null) {
            throw new ValidationException("stayPeriod is required");
        }
        List<ReservationServiceItem> items = serviceItems == null ? List.of() : serviceItems;
        Money basePrice = roomType.basePrice().multiply(stayPeriod.nights());
        return items.stream()
                .map(ReservationServiceItem::totalPrice)
                .reduce(basePrice, Money::plus);
    }

    private Money normalizeDiscount(Money discountAmount, Money subtotal) {
        if (discountAmount == null) {
            return Money.zero(subtotal.currency().getCurrencyCode());
        }
        if (!discountAmount.currency().equals(subtotal.currency())) {
            throw new ValidationException("discount currency must match subtotal currency");
        }
        if (discountAmount.amount().compareTo(subtotal.amount()) > 0) {
            throw new ValidationException("discount must not exceed subtotal");
        }
        return discountAmount;
    }
}
