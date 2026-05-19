package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomTypeFeaturesTest {

    @Test
    void shouldNormalizeBlankBedSetupAndAmenities() {
        var features = new RoomTypeFeatures(" ", null, null);

        assertNull(features.bedSetup());
        assertEquals(Set.of(), features.amenities());
    }

    @Test
    void shouldTrimBedSetupAndKeepAmenities() {
        var features = new RoomTypeFeatures(
                " 1 king bed ",
                new BigDecimal("28.5"),
                Set.of(RoomAmenity.WIFI, RoomAmenity.PRIVATE_BATHROOM)
        );

        assertEquals("1 king bed", features.bedSetup());
        assertEquals(new BigDecimal("28.5"), features.roomSizeSqm());
        assertEquals(Set.of(RoomAmenity.WIFI, RoomAmenity.PRIVATE_BATHROOM), features.amenities());
    }

    @Test
    void shouldRejectOversizedBedSetup() {
        assertThrows(
                ValidationException.class,
                () -> new RoomTypeFeatures("x".repeat(121), null, Set.of())
        );
    }

    @Test
    void shouldRejectInvalidRoomSize() {
        assertThrows(
                ValidationException.class,
                () -> new RoomTypeFeatures(null, BigDecimal.ZERO, Set.of())
        );
        assertThrows(
                ValidationException.class,
                () -> new RoomTypeFeatures(null, new BigDecimal("1000.1"), Set.of())
        );
    }
}
