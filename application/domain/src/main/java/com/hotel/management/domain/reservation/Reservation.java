package com.hotel.management.domain.reservation;

import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.StayPeriod;

import java.time.Instant;
import java.time.LocalDate;

public final class Reservation {

    private final String id;
    private final Long hotelId;
    private final Long roomId;
    private final Long roomTypeId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final AccommodationParty accommodationParty;
    private final ReservationStatus status;
    private final Instant createdAt;
    private final Instant cancelledAt;
    private final String createdBy;

    private Reservation(
            String id,
            Long hotelId,
            Long roomId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        this.id = requireText(id, "reservationId is required");
        this.hotelId = require(hotelId, "hotelId is required");
        this.roomId = roomId;
        this.roomTypeId = require(roomTypeId, "roomTypeId is required");
        this.checkIn = require(checkIn, "checkIn is required");
        this.checkOut = require(checkOut, "checkOut is required");
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("checkOut must be after checkIn");
        }
        this.accommodationParty = require(accommodationParty, "accommodationParty is required");
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
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            Instant createdAt,
            String createdBy
    ) {
        return new Reservation(
                id,
                hotelId,
                null,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
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
            AccommodationParty accommodationParty,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        return rehydrate(id, hotelId, null, roomTypeId, checkIn, checkOut, accommodationParty, status, createdAt, cancelledAt, createdBy);
    }

    public static Reservation rehydrate(
            String id,
            Long hotelId,
            Long roomId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            AccommodationParty accommodationParty,
            ReservationStatus status,
            Instant createdAt,
            Instant cancelledAt,
            String createdBy
    ) {
        return new Reservation(id, hotelId, roomId, roomTypeId, checkIn, checkOut, accommodationParty, status, createdAt, cancelledAt, createdBy);
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
                roomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
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
                assignedRoomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
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
                roomId,
                roomTypeId,
                checkIn,
                checkOut,
                accommodationParty,
                ReservationStatus.CHECKED_OUT,
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
