package com.hotel.management.service.reservation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.hotel.management.domain.shared.value.PetDetails;

public record CreateReservationResult(
        String reservationId,
        Long hotelId,
        Long roomId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int adults,
        List<Integer> childrenAges,
        List<PetDetails> pets,
        String status,
        Instant createdAt,
        Instant cancelledAt,
        String createdBy
) {
}
