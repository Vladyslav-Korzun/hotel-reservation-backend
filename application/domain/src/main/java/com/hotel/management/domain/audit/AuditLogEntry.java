package com.hotel.management.domain.audit;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.time.Instant;

public final class AuditLogEntry {

    private final Long id;
    private final String actorId;
    private final String actorRole;
    private final AuditActionType actionType;
    private final AuditEntityType entityType;
    private final String entityId;
    private final Instant timestamp;
    private final String details;

    public AuditLogEntry(
            Long id,
            String actorId,
            String actorRole,
            AuditActionType actionType,
            AuditEntityType entityType,
            String entityId,
            Instant timestamp,
            String details
    ) {
        this.id = id;
        this.actorId = requireText(actorId, "actorId is required");
        this.actorRole = requireText(actorRole, "actorRole is required");
        this.actionType = require(actionType, "actionType is required");
        this.entityType = require(entityType, "entityType is required");
        this.entityId = requireText(entityId, "entityId is required");
        this.timestamp = require(timestamp, "timestamp is required");
        this.details = details;
    }

    public Long id() {
        return id;
    }

    public String actorId() {
        return actorId;
    }

    public String actorRole() {
        return actorRole;
    }

    public AuditActionType actionType() {
        return actionType;
    }

    public AuditEntityType entityType() {
        return entityType;
    }

    public String entityId() {
        return entityId;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public String details() {
        return details;
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
        return value.trim();
    }
}
