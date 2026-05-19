package com.hotel.management.controller;

import com.hotel.management.api.dto.AvailableRoomResponse;
import com.hotel.management.api.dto.SearchAvailableRoomsRequest;
import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.StayPeriod;
import com.hotel.management.mapper.AvailabilityMapper;
import com.hotel.management.domain.room.AvailableRoomResult;
import com.hotel.management.service.availability.SearchAvailabilityFacade;
import com.hotel.management.service.availability.SearchAvailableRoomsCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomsControllerTest {

    private final TestSearchAvailabilityFacade searchAvailabilityFacade = new TestSearchAvailabilityFacade();
    private final TestAvailabilityMapper availabilityMapper = new TestAvailabilityMapper();
    private final RoomsController controller = new RoomsController(searchAvailabilityFacade, availabilityMapper);

    @Test
    void shouldSearchAvailableRooms() {
        var request = new SearchAvailableRoomsRequest()
                .city("Bratislava")
                .checkIn(LocalDate.of(2026, 6, 1))
                .checkOut(LocalDate.of(2026, 6, 3))
                .adults(2)
                .childrenAges(List.of(7));
        availabilityMapper.command = new SearchAvailableRoomsCommand(
                "Bratislava",
                null,
                new StayPeriod(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3)),
                new AccommodationParty(new GuestComposition(2, List.of(7)), List.of())
        );
        availabilityMapper.responses = List.of(new AvailableRoomResponse().hotelId(1L));

        var actual = controller.searchAvailableRooms(request);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(availabilityMapper.responses);
        assertThat(availabilityMapper.request).isSameAs(request);
        assertThat(searchAvailabilityFacade.command).isSameAs(availabilityMapper.command);
        assertThat(availabilityMapper.results).isSameAs(searchAvailabilityFacade.results);
    }

    private static final class TestSearchAvailabilityFacade implements SearchAvailabilityFacade {

        private final List<AvailableRoomResult> results = List.of(new AvailableRoomResult(
                1L,
                "Hotel Danube",
                2L,
                "Deluxe",
                2,
                1,
                1,
                3,
                true,
                1,
                Money.of("120.00", "EUR"),
                4,
                "1 king bed",
                new java.math.BigDecimal("28.5"),
                java.util.Set.of(RoomAmenity.WIFI)
        ));
        private SearchAvailableRoomsCommand command;

        @Override
        public List<AvailableRoomResult> searchAvailableRooms(SearchAvailableRoomsCommand command) {
            this.command = command;
            return results;
        }
    }

    private static final class TestAvailabilityMapper extends AvailabilityMapper {

        private SearchAvailableRoomsRequest request;
        private SearchAvailableRoomsCommand command;
        private List<AvailableRoomResult> results;
        private List<AvailableRoomResponse> responses = List.of();

        @Override
        public SearchAvailableRoomsCommand toCommand(SearchAvailableRoomsRequest request) {
            this.request = request;
            return command;
        }

        @Override
        public List<AvailableRoomResponse> toResponse(List<AvailableRoomResult> result) {
            this.results = result;
            return responses;
        }
    }
}