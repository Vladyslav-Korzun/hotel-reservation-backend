package com.hotel.management.service.staff;

import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationServiceItemResult;
import com.hotel.management.domain.reservation.StaffReservationResult;

public class StaffReservationResultMapper {

    public StaffReservationResult toResult(Reservation reservation) {
        return new StaffReservationResult(
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
                reservation.contactEmail() == null ? null : reservation.contactEmail().value(),
                reservation.contactPhone(),
                reservation.specialRequests(),
                reservation.basePrice(),
                reservation.servicesPrice(),
                reservation.discountAmount(),
                reservation.finalPrice(),
                reservation.serviceItems().stream()
                        .map(item -> new com.hotel.management.domain.reservation.ReservationServiceItemResult(
                                item.serviceOfferingId(),
                                item.serviceNameSnapshot(),
                                item.priceSnapshot(),
                                item.quantity(),
                                item.totalPrice()
                        ))
                        .toList(),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.cancelledAt(),
                reservation.createdBy()
        );
    }
}