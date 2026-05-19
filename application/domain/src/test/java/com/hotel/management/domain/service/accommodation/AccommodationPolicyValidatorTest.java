package com.hotel.management.domain.service.accommodation;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccommodationPolicyValidatorTest {

    private final AccommodationPolicyValidator validator = new AccommodationPolicyValidator();

    @Test
    void shouldCountTeenMinorAsAdultEquivalentForOccupancy() {
        assertDoesNotThrow(() -> validator.validate(
                hotel(new HotelPolicy(true, false, 2, 12, 13)),
                roomType(new OccupancyPolicy(3, 0, 1, 3)),
                party(2, 17)
        ));
    }

    @Test
    void shouldRejectTeenMinorWhenAdultSlotsAreExceeded() {
        assertThrows(ValidationException.class, () -> validator.validate(
                hotel(new HotelPolicy(true, false, 2, 12, 13)),
                roomType(new OccupancyPolicy(2, 0, 1, 2)),
                party(2, 17)
        ));
    }

    @Test
    void shouldRejectAdultAgeSubmittedAsChildAge() {
        assertThrows(ValidationException.class, () -> validator.validate(
                hotel(new HotelPolicy(true, false, 2, 12, 13)),
                roomType(new OccupancyPolicy(3, 1, 1, 4)),
                party(2, 18)
        ));
    }

    @Test
    void shouldNotChargeInfantsButShouldChargeChildrenAndAdultEquivalentMinors() {
        int chargeableGuests = validator.chargeableGuestCount(
                new HotelPolicy(true, false, 2, 12, 13),
                party(2, 1, 10, 17)
        );

        assertEquals(4, chargeableGuests);
    }

    private static Hotel hotel(HotelPolicy policy) {
        return new Hotel(
                1L,
                "Danube Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                HotelStatus.ACTIVE,
                policy
        );
    }

    private static RoomType roomType(OccupancyPolicy occupancyPolicy) {
        return new RoomType(
                10L,
                1L,
                "Standard",
                occupancyPolicy,
                new PetPolicy(false, 0, Set.of(), null, null),
                Money.of("100.00", "EUR"),
                "Standard room",
                RoomTypeFeatures.empty()
        );
    }

    private static AccommodationParty party(int adults, Integer... childrenAges) {
        return new AccommodationParty(new GuestComposition(adults, List.of(childrenAges)), List.of());
    }
}
