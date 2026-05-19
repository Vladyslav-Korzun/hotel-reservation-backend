package com.hotel.management.service.reservation;

import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.domain.reservation.ReservationServiceItemResult;

public class ReservationResultMapper {

    public CreateReservationResult toCreateResult(Reservation reservation) {
        return new CreateReservationResult(
                reservation.id(),
                reservation.hotelId(),
                reservation.guestId(),
                reservation.roomId(),
                reservation.roomTypeId(),
                reservation.checkIn(),
                reservation.checkOut(),
                reservation.accommodationParty().guests().adults(),
                reservation.accommodationParty().guests().childrenAges(),
                reservation.accommodationParty().pets(),
                emailValue(reservation),
                reservation.contactPhone(),
                reservation.specialRequests(),
                reservation.basePrice(),
                reservation.servicesPrice(),
                reservation.discountAmount(),
                reservation.finalPrice(),
                toServiceItemResults(reservation),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.cancelledAt(),
                reservation.createdBy()
        );
    }

    public GetReservationResult toGetResult(Reservation reservation) {
        return new GetReservationResult(
                reservation.id(),
                reservation.hotelId(),
                reservation.guestId(),
                reservation.roomId(),
                reservation.roomTypeId(),
                reservation.checkIn(),
                reservation.checkOut(),
                reservation.accommodationParty().guests().adults(),
                reservation.accommodationParty().guests().childrenAges(),
                reservation.accommodationParty().pets(),
                emailValue(reservation),
                reservation.contactPhone(),
                reservation.specialRequests(),
                reservation.basePrice(),
                reservation.servicesPrice(),
                reservation.discountAmount(),
                reservation.finalPrice(),
                toServiceItemResults(reservation),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.cancelledAt(),
                reservation.createdBy()
        );
    }

    private java.util.List<ReservationServiceItemResult> toServiceItemResults(Reservation reservation) {
        return reservation.serviceItems().stream()
                .map(item -> new ReservationServiceItemResult(
                        item.serviceOfferingId(),
                        item.serviceNameSnapshot(),
                        item.priceSnapshot(),
                        item.quantity(),
                        item.totalPrice()
                ))
                .toList();
    }

    private String emailValue(Reservation reservation) {
        return reservation.contactEmail() == null ? null : reservation.contactEmail().value();
    }
}