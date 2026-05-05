package com.hotel.management.service.availability;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.StayPeriod;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.service.reservation.ActiveReservationView;
import com.hotel.management.service.reservation.ReservationQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAvailabilityServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private ReservationQueryPort reservationQueryPort;

    @Mock
    private AccommodationPolicyValidator accommodationPolicyValidator;

    @InjectMocks
    private SearchAvailabilityService searchAvailabilityService;

    @Test
    void shouldReturnAvailableRoomTypesByCity() {
        var period = new StayPeriod(LocalDate.parse("2026-05-10"), LocalDate.parse("2026-05-12"));
        var hotel = activeHotel(1L, "Danube Hotel", "Bratislava");
        var standard = roomType(10L, 1L, "Standard", 2, 0, 0, 2);
        var family = roomType(20L, 1L, "Family", 2, 2, 1, 4);
        var overlappingReservation = new ActiveReservationView(1L, 10L);

        when(hotelRepository.findActiveByCity("Bratislava")).thenReturn(List.of(hotel));
        when(roomTypeRepository.findByHotelIds(List.of(1L))).thenReturn(List.of(standard, family));
        when(roomRepository.findByHotelIds(List.of(1L))).thenReturn(List.of(
                room(100L, 1L, "101", 10L, 2, RoomStatus.AVAILABLE),
                room(101L, 1L, "102", 10L, 2, RoomStatus.AVAILABLE),
                room(102L, 1L, "103", 10L, 2, RoomStatus.MAINTENANCE),
                room(200L, 1L, "201", 20L, 4, RoomStatus.AVAILABLE)
        ));
        when(reservationQueryPort.findActiveOverlapping(List.of(1L), period)).thenReturn(List.of(overlappingReservation));

        var result = searchAvailabilityService.searchAvailableRooms(SearchAvailableRoomsCommand.byCity(
                "Bratislava",
                period,
                party(2)
        ));

        assertEquals(2, result.size());
        assertEquals(10L, result.getFirst().roomTypeId());
        assertEquals(1, result.getFirst().availableCount());
        assertEquals(20L, result.get(1).roomTypeId());
        assertEquals(1, result.get(1).availableCount());
    }

    @Test
    void shouldReturnAvailableRoomTypesAcrossAllActiveHotelsWhenLocationIsMissing() {
        var period = new StayPeriod(LocalDate.parse("2026-05-10"), LocalDate.parse("2026-05-12"));
        var hotelOne = activeHotel(1L, "Danube Hotel", "Bratislava");
        var hotelTwo = activeHotel(2L, "Tatra Hotel", "Kosice");
        var standard = roomType(10L, 1L, "Standard", 2, 0, 0, 2);
        var studio = roomType(20L, 2L, "Studio", 2, 1, 1, 3);

        when(hotelRepository.findAllActive()).thenReturn(List.of(hotelOne, hotelTwo));
        when(roomTypeRepository.findByHotelIds(List.of(1L, 2L))).thenReturn(List.of(standard, studio));
        when(roomRepository.findByHotelIds(List.of(1L, 2L))).thenReturn(List.of(
                room(100L, 1L, "101", 10L, 2, RoomStatus.AVAILABLE),
                room(200L, 2L, "201", 20L, 2, RoomStatus.AVAILABLE)
        ));
        when(reservationQueryPort.findActiveOverlapping(List.of(1L, 2L), period)).thenReturn(List.of());

        var result = searchAvailabilityService.searchAvailableRooms(new SearchAvailableRoomsCommand(
                null,
                null,
                period,
                party(1)
        ));

        assertEquals(2, result.size());
        assertEquals(1L, result.getFirst().hotelId());
        assertEquals(2L, result.get(1).hotelId());
    }

    @Test
    void shouldExcludeRoomTypeWhenAllInventoryIsBlockedByReservations() {
        var period = new StayPeriod(LocalDate.parse("2026-05-10"), LocalDate.parse("2026-05-12"));
        var hotel = activeHotel(1L, "Danube Hotel", "Bratislava");
        var standard = roomType(10L, 1L, "Standard", 2, 0, 0, 2);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotelIds(List.of(1L))).thenReturn(List.of(standard));
        when(roomRepository.findByHotelIds(List.of(1L))).thenReturn(List.of(
                room(100L, 1L, "101", 10L, 2, RoomStatus.AVAILABLE)
        ));
        when(reservationQueryPort.findActiveOverlapping(List.of(1L), period)).thenReturn(List.of(
                new ActiveReservationView(1L, 10L)
        ));

        var result = searchAvailabilityService.searchAvailableRooms(SearchAvailableRoomsCommand.byHotel(
                1L,
                period,
                party(2)
        ));

        assertEquals(0, result.size());
    }

    @Test
    void shouldRejectUnknownHotel() {
        var period = new StayPeriod(LocalDate.parse("2026-05-10"), LocalDate.parse("2026-05-12"));
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> searchAvailabilityService.searchAvailableRooms(
                SearchAvailableRoomsCommand.byHotel(999L, period, party(2))
        ));
    }

    private static AccommodationParty party(int adults, Integer... childrenAges) {
        return new AccommodationParty(new GuestComposition(adults, List.of(childrenAges)), List.of());
    }

    private static Hotel activeHotel(Long id, String name, String city) {
        return new Hotel(
                id,
                name,
                city,
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                HotelStatus.ACTIVE,
                new HotelPolicy(true, true, 2, 11)
        );
    }

    private static RoomType roomType(Long id, Long hotelId, String name, int maxAdults, int maxChildren, int maxInfants, int maxTotalGuests) {
        return new RoomType(
                id,
                hotelId,
                name,
                new OccupancyPolicy(maxAdults, maxChildren, maxInfants, maxTotalGuests),
                new PetPolicy(false, 0, java.util.Set.of(), null, null),
                Money.of("100.00", "EUR"),
                name + " room"
        );
    }

    private static Room room(Long id, Long hotelId, String number, Long roomTypeId, int capacity, RoomStatus status) {
        return new Room(id, hotelId, number, roomTypeId, capacity, status);
    }
}
