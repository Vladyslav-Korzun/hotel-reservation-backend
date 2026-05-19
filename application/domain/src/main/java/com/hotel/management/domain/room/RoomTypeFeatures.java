package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record RoomTypeFeatures(
        String bedSetup,
        BigDecimal roomSizeSqm,
        Set<RoomAmenity> amenities
) {

    private static final int BED_SETUP_MAX_LENGTH = 120;
    private static final BigDecimal ROOM_SIZE_MAX = new BigDecimal("1000");

    public RoomTypeFeatures {
        bedSetup = normalizeBedSetup(bedSetup);
        if (roomSizeSqm != null && (roomSizeSqm.signum() <= 0 || roomSizeSqm.compareTo(ROOM_SIZE_MAX) > 0)) {
            throw new ValidationException("roomSizeSqm must be between 1 and 1000");
        }
        amenities = normalizeAmenities(amenities);
    }

    public static RoomTypeFeatures empty() {
        return new RoomTypeFeatures(null, null, Set.of());
    }

    private static String normalizeBedSetup(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > BED_SETUP_MAX_LENGTH) {
            throw new ValidationException("bedSetup length must be less than or equal to 120");
        }
        return normalized;
    }

    private static Set<RoomAmenity> normalizeAmenities(Set<RoomAmenity> value) {
        if (value == null || value.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(value));
    }
}
