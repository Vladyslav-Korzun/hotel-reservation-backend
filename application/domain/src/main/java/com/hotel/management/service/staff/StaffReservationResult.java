package com.hotel.management.service.staff;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.service.reservation.ReservationServiceItemResult;

public record StaffReservationResult(
        String reservationId,
        Long hotelId,
        Long guestId,
        Long roomId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int adults,
        List<Integer> childrenAges,
        List<PetDetails> pets,
        String contactEmail,
        String contactPhone,
        String specialRequests,
        Money basePrice,
        Money servicesPrice,
        Money discountAmount,
        Money finalPrice,
        List<ReservationServiceItemResult> serviceItems,
        String status,
        Instant createdAt,
        Instant cancelledAt,
        String createdBy
) {
}
