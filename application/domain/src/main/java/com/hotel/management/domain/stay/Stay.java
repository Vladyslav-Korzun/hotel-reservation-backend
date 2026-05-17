package com.hotel.management.domain.stay;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.time.Instant;

public final class Stay {

    private final Long id;
    private final String reservationId;
    private final Long roomId;
    private final Instant checkedInAt;
    private final Instant checkedOutAt;
    private final StayStatus status;

    private Stay(
            Long id,
            String reservationId,
            Long roomId,
            Instant checkedInAt,
            Instant checkedOutAt,
            StayStatus status
    ) {
        this.id = id;
        this.reservationId = requireText(reservationId, "reservationId is required");
        this.roomId = require(roomId, "roomId is required");
        this.checkedInAt = require(checkedInAt, "checkedInAt is required");
        this.status = require(status, "stayStatus is required");
        if (status == StayStatus.ACTIVE && checkedOutAt != null) {
            throw new ValidationException("Active stay cannot have checkedOutAt");
        }
        if (status == StayStatus.COMPLETED && checkedOutAt == null) {
            throw new ValidationException("Completed stay requires checkedOutAt");
        }
        if (checkedOutAt != null && checkedOutAt.isBefore(checkedInAt)) {
            throw new ValidationException("checkedOutAt cannot be before checkedInAt");
        }
        this.checkedOutAt = checkedOutAt;
    }

    public static Stay start(Long id, String reservationId, Long roomId, Instant checkedInAt) {
        return new Stay(id, reservationId, roomId, checkedInAt, null, StayStatus.ACTIVE);
    }

    public static Stay rehydrate(
            Long id,
            String reservationId,
            Long roomId,
            Instant checkedInAt,
            Instant checkedOutAt,
            StayStatus status
    ) {
        return new Stay(id, reservationId, roomId, checkedInAt, checkedOutAt, status);
    }

    public Stay complete(Instant checkedOutAt) {
        require(checkedOutAt, "checkedOutAt is required");
        if (status != StayStatus.ACTIVE) {
            throw new ValidationException("Only active stay can be completed");
        }
        return new Stay(id, reservationId, roomId, checkedInAt, checkedOutAt, StayStatus.COMPLETED);
    }

    public Long id() {
        return id;
    }

    public String reservationId() {
        return reservationId;
    }

    public Long roomId() {
        return roomId;
    }

    public Instant checkedInAt() {
        return checkedInAt;
    }

    public Instant checkedOutAt() {
        return checkedOutAt;
    }

    public StayStatus status() {
        return status;
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
