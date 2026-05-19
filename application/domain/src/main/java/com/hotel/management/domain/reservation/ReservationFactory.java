package com.hotel.management.domain.reservation;

import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.EmailAddress;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class ReservationFactory {

    public Reservation createPendingReservation(
            String reservationId,
            Long hotelId,
            Long guestId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            ReservationPriceSnapshot priceSnapshot,
            List<ReservationServiceItem> serviceItems,
            Instant createdAt,
            String createdBy
    ) {
        return createPendingReservation(
                reservationId,
                hotelId,
                guestId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                null,
                null,
                null,
                priceSnapshot,
                serviceItems,
                createdAt,
                createdBy
        );
    }

    public Reservation createPendingReservation(
            String reservationId,
            Long hotelId,
            Long guestId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            EmailAddress contactEmail,
            String contactPhone,
            String specialRequests,
            ReservationPriceSnapshot priceSnapshot,
            List<ReservationServiceItem> serviceItems,
            Instant createdAt,
            String createdBy
    ) {
        return Reservation.createPending(
                reservationId,
                hotelId,
                guestId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                contactEmail,
                contactPhone,
                specialRequests,
                priceSnapshot,
                serviceItems,
                createdAt,
                createdBy
        );
    }

    public List<ReservationServiceItem> createServiceItems(
            String reservationId,
            List<ServiceOffering> serviceOfferings,
            List<ServiceOfferingSelection> selections
    ) {
        if (selections == null || selections.isEmpty()) {
            return List.of();
        }
        if (serviceOfferings == null) {
            throw new ValidationException("serviceOfferings are required");
        }
        return selections.stream()
                .map(selection -> ReservationServiceItem.snapshot(
                        reservationId,
                        findServiceOffering(serviceOfferings, selection.serviceOfferingId()),
                        selection.quantity()
                ))
                .toList();
    }

    private ServiceOffering findServiceOffering(List<ServiceOffering> serviceOfferings, Long serviceOfferingId) {
        return serviceOfferings.stream()
                .filter(candidate -> candidate.id().equals(serviceOfferingId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Selected service offering was not loaded: " + serviceOfferingId));
    }
}
