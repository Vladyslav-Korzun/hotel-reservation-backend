package com.hotel.reservation.reservation;

import com.hotel.reservation.shared.exception.ForbiddenException;
import com.hotel.reservation.shared.exception.ValidationException;

import java.time.Instant;
import java.time.LocalDate;

public final class Reservation {

    private final String id;
    private final Long hotelId;
    private final Long roomTypeId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guestCount;
    private final ReservationStatus status;
    private final Instant createdAt;
    private final Instant cancelledAt;
    private final String createdBy;

    private Reservation(
            String id,
            Long hotelId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guestCount,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        this.id = requireText(id, "reservationId is required");
        this.hotelId = require(hotelId, "hotelId is required");
        this.roomTypeId = require(roomTypeId, "roomTypeId is required");
        this.checkIn = require(checkIn, "checkIn is required");
        this.checkOut = require(checkOut, "checkOut is required");
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("checkOut must be after checkIn");
        }
        if (guestCount <= 0) {
            throw new ValidationException("guestCount must be greater than zero");
        }
        this.guestCount = guestCount;
        this.status = require(status, "status is required");
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
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guestCount,
            Instant createdAt,
            String createdBy
    ) {
        return new Reservation(
                id,
                hotelId,
                roomTypeId,
                checkIn,
                checkOut,
                guestCount,
                ReservationStatus.PENDING,
                createdAt,
                null,
                createdBy
        );
    }

    public static Reservation rehydrate(
            String id,
            Long hotelId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guestCount,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        return new Reservation(id, hotelId, roomTypeId, checkIn, checkOut, guestCount, status, createdAt, cancelledAt, createdBy);
    }

    public boolean isOwnedBy(String actorId) {
        return createdBy.equals(requireText(actorId, "actorId is required"));
    }

    public void assertOwnedBy(String actorId) {
        if (isOwnedBy(actorId)) {
            return;
        }
        throw new ForbiddenException("Access to this reservation is denied");
    }

    public Reservation cancel(Instant cancelledAt) {
        require(cancelledAt, "cancelledAt is required");
        if (status == ReservationStatus.CANCELLED) {
            throw new ValidationException("Reservation is already cancelled");
        }
        return new Reservation(
                id,
                hotelId,
                roomTypeId,
                checkIn,
                checkOut,
                guestCount,
                ReservationStatus.CANCELLED,
                createdAt,
                cancelledAt,
                createdBy
        );
    }

    public String id() {
        return id;
    }

    public Long hotelId() {
        return hotelId;
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

    public int guestCount() {
        return guestCount;
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
}
