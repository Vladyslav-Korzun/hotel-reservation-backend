package com.hotel.management.domain.audit;

import com.hotel.management.domain.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditLogEntryTest {

    @Test
    void shouldCreateAuditLogEntry() {
        AuditLogEntry entry = new AuditLogEntry(
                null,
                "staff-1",
                "STAFF",
                AuditActionType.CHECK_IN,
                AuditEntityType.RESERVATION,
                "reservation-1",
                Instant.parse("2026-05-10T10:00:00Z"),
                "Checked in reservation"
        );

        assertEquals("staff-1", entry.actorId());
        assertEquals(AuditActionType.CHECK_IN, entry.actionType());
    }

    @Test
    void shouldRequireActorActionEntityAndTimestamp() {
        assertThrows(ValidationException.class, () -> new AuditLogEntry(
                null,
                "",
                "STAFF",
                AuditActionType.CHECK_IN,
                AuditEntityType.RESERVATION,
                "reservation-1",
                Instant.parse("2026-05-10T10:00:00Z"),
                null
        ));
    }
}
