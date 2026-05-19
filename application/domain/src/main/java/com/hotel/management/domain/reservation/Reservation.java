package com.hotel.management.domain.reservation;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.EmailAddress;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.StayPeriod;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class Reservation {

    private final String id;
    private final Long hotelId;
    private final Long guestId;
    private final Long roomId;
    private final Long roomTypeId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final AccommodationParty accommodationParty;
    private final EmailAddress contactEmail;
    private final String contactPhone;
    private final String specialRequests;
    private final ReservationPriceSnapshot priceSnapshot;
    private final List<ReservationServiceItem> serviceItems;
    private final ReservationStatus status;
    private final Instant createdAt;
    private final Instant cancelledAt;
    private final String createdBy;

    private Reservation(
            String id,
            Long hotelId,
            Long guestId,
            Long roomId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            EmailAddress contactEmail,
            String contactPhone,
            String specialRequests,
            ReservationPriceSnapshot priceSnapshot,
            List<ReservationServiceItem> serviceItems,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        this.id = requireText(id, "reservationId is required");
        this.hotelId = require(hotelId, "hotelId is required");
        this.guestId = require(guestId, "guestId is required");
        this.roomId = roomId;
        this.roomTypeId = require(roomTypeId, "roomTypeId is required");
        this.checkIn = require(checkIn, "checkIn is required");
        this.checkOut = require(checkOut, "checkOut is required");
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("checkOut must be after checkIn");
        }
        this.accommodationParty = require(accommodationParty, "accommodationParty is required");
        this.contactEmail = contactEmail;
        this.contactPhone = normalizeContactPhone(contactPhone);
        this.specialRequests = normalizeSpecialRequests(specialRequests);
        this.priceSnapshot = require(priceSnapshot, "priceSnapshot is required");
        this.serviceItems = List.copyOf(require(serviceItems, "serviceItems is required"));
        this.status = require(status, "status is required");
        if ((status == ReservationStatus.CHECKED_IN || status == ReservationStatus.CHECKED_OUT) && roomId == null) {
            throw new ValidationException("roomId is required for checked-in or checked-out reservation");
        }
        this.createdAt = require(createdAt, "createdAt is required");
        if (status == ReservationStatus.CANCELLED && cancelledAt == null) {
            throw new ValidationException("cancelledAt is required for cancelled reservation");
        }
        if (status != ReservationStatus.CANCELLED && cancelledAt != null) {
            throw new ValidationException("cancelledAt is allowed only for cancelled reservation");
        }
        this.cancelledAt = cancelledAt;
        this.createdBy = requireText(createdBy, "createdBy is required");
    }

    public static Reservation createPending(
            String id,
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
        return createPending(
                id,
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

    public static Reservation createPending(
            String id,
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
        return new Reservation(
                id,
                hotelId,
                require(guestId, "guestId is required"),
                null,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                contactEmail,
                contactPhone,
                specialRequests,
                priceSnapshot,
                serviceItems,
                ReservationStatus.PENDING,
                createdAt,
                null,
                createdBy
        );
    }

    public static Reservation rehydrate(
            String id,
            Long hotelId,
            Long guestId,
            Long roomId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            ReservationPriceSnapshot priceSnapshot,
            List<ReservationServiceItem> serviceItems,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        return rehydrate(
                id,
                hotelId,
                guestId,
                roomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                null,
                null,
                null,
                priceSnapshot,
                serviceItems,
                status,
                createdAt,
                cancelledAt,
                createdBy
        );
    }

    public static Reservation rehydrate(
            String id,
            Long hotelId,
            Long guestId,
            Long roomId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            EmailAddress contactEmail,
            String contactPhone,
            String specialRequests,
            ReservationPriceSnapshot priceSnapshot,
            List<ReservationServiceItem> serviceItems,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        return new Reservation(
                id,
                hotelId,
                guestId,
                roomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                contactEmail,
                contactPhone,
                specialRequests,
                priceSnapshot,
                serviceItems,
                status,
                createdAt,
                cancelledAt,
                createdBy
        );
    }

    public boolean belongsTo(String actorId) {
        return createdBy.equals(requireText(actorId, "actorId is required"));
    }

    public Reservation cancel(Instant cancelledAt) {
        require(cancelledAt, "cancelledAt is required");
        if (status == ReservationStatus.CANCELLED) {
            throw new ValidationException("Reservation is already cancelled");
        }
        if (status == ReservationStatus.CHECKED_IN) {
            throw new ValidationException("Checked-in reservation cannot be cancelled");
        }
        if (status == ReservationStatus.CHECKED_OUT) {
            throw new ValidationException("Checked-out reservation cannot be cancelled");
        }
        if (status == ReservationStatus.NO_SHOW) {
            throw new ValidationException("No-show reservation cannot be cancelled");
        }
        return new Reservation(
                id,
                hotelId,
                guestId,
                roomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                contactEmail,
                contactPhone,
                specialRequests,
                priceSnapshot,
                serviceItems,
                ReservationStatus.CANCELLED,
                createdAt,
                cancelledAt,
                createdBy
        );
    }

    public Reservation checkIn(Long assignedRoomId) {
        require(assignedRoomId, "roomId is required");
        if (status == ReservationStatus.CANCELLED) {
            throw new ValidationException("Cancelled reservation cannot be checked in");
        }
        if (status == ReservationStatus.CHECKED_IN) {
            throw new ValidationException("Reservation is already checked in");
        }
        if (status == ReservationStatus.CHECKED_OUT) {
            throw new ValidationException("Checked out reservation cannot be checked in");
        }
        if (status == ReservationStatus.NO_SHOW) {
            throw new ValidationException("No-show reservation cannot be checked in");
        }
        return new Reservation(
                id,
                hotelId,
                guestId,
                assignedRoomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                contactEmail,
                contactPhone,
                specialRequests,
                priceSnapshot,
                serviceItems,
                ReservationStatus.CHECKED_IN,
                createdAt,
                cancelledAt,
                createdBy
        );
    }

    public Reservation completeCheckOut() {
        if (status != ReservationStatus.CHECKED_IN) {
            throw new ValidationException("Only checked-in reservation can be checked out");
        }
        if (roomId == null) {
            throw new ValidationException("Checked-in reservation must have assigned room");
        }
        return new Reservation(
                id,
                hotelId,
                guestId,
                roomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                contactEmail,
                contactPhone,
                specialRequests,
                priceSnapshot,
                serviceItems,
                ReservationStatus.CHECKED_OUT,
                createdAt,
                cancelledAt,
                createdBy
        );
    }

    public Reservation markNoShow() {
        if (status == ReservationStatus.CANCELLED) {
            throw new ValidationException("Cancelled reservation cannot be marked as no-show");
        }
        if (status == ReservationStatus.CHECKED_IN) {
            throw new ValidationException("Checked-in reservation cannot be marked as no-show");
        }
        if (status == ReservationStatus.CHECKED_OUT) {
            throw new ValidationException("Checked-out reservation cannot be marked as no-show");
        }
        if (status == ReservationStatus.NO_SHOW) {
            throw new ValidationException("Reservation is already marked as no-show");
        }
        return new Reservation(
                id,
                hotelId,
                guestId,
                roomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                contactEmail,
                contactPhone,
                specialRequests,
                priceSnapshot,
                serviceItems,
                ReservationStatus.NO_SHOW,
                createdAt,
                cancelledAt,
                createdBy
        );
    }

    public StayPeriod stayPeriod() {
        return new StayPeriod(checkIn, checkOut);
    }

    public boolean overlaps(StayPeriod stayPeriod) {
        return stayPeriod().overlaps(stayPeriod);
    }

    public boolean isActiveForAvailability() {
        return status == ReservationStatus.PENDING
                || status == ReservationStatus.CONFIRMED
                || status == ReservationStatus.CHECKED_IN;
    }

    public String id() {
        return id;
    }

    public Long hotelId() {
        return hotelId;
    }

    public Long guestId() {
        return guestId;
    }

    public Long roomId() {
        return roomId;
    }

    public Long roomTypeId() {
        return roomTypeId;
    }

    public LocalDate checkIn() {
        return checkIn;
    }

    public LocalDate checkOut() {
        return checkOut;
    }

    public AccommodationParty accommodationParty() {
        return accommodationParty;
    }

    public EmailAddress contactEmail() {
        return contactEmail;
    }

    public String contactPhone() {
        return contactPhone;
    }

    public String specialRequests() {
        return specialRequests;
    }

    public ReservationPriceSnapshot priceSnapshot() {
        return priceSnapshot;
    }

    public List<ReservationServiceItem> serviceItems() {
        return serviceItems;
    }

    public Money basePrice() {
        return priceSnapshot.basePrice();
    }

    public Money servicesPrice() {
        return priceSnapshot.servicesPrice();
    }

    public Money discountAmount() {
        return priceSnapshot.discountAmount();
    }

    public Money finalPrice() {
        return priceSnapshot.finalPrice();
    }

    public ReservationStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant cancelledAt() {
        return cancelledAt;
    }

    public String createdBy() {
        return createdBy;
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
        return value;
    }

    private static String normalizeContactPhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 32) {
            throw new ValidationException("contactPhone must not exceed 32 characters");
        }
        return normalized;
    }

    private static String normalizeSpecialRequests(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 500) {
            throw new ValidationException("specialRequests must not exceed 500 characters");
        }
        return normalized;
    }

}
