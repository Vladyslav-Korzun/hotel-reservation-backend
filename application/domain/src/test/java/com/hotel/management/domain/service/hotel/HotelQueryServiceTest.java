package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hotel.management.domain.service.mapper.HotelQueryResultMapper;

@ExtendWith(MockitoExtension.class)
class HotelQueryServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ServiceOfferingRepository serviceOfferingRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Test
    void shouldListAllActiveHotelsWhenCityIsMissing() {
        var service = service();
        when(hotelRepository.findAllActive()).thenReturn(List.of(activeHotel(1L, "Danube Hotel", "Bratislava")));

        var result = service.listHotels(new ListHotelsQuery(null));

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().hotelId());
        assertEquals("ACTIVE", result.getFirst().status());
        verify(hotelRepository, never()).findActiveByCity(null);
    }

    @Test
    void shouldListActiveHotelsByCity() {
        var service = service();
        when(hotelRepository.findActiveByCity("Bratislava"))
                .thenReturn(List.of(activeHotel(1L, "Danube Hotel", "Bratislava")));

        var result = service.listHotels(new ListHotelsQuery(" Bratislava "));

        assertEquals(1, result.size());
        assertEquals("Bratislava", result.getFirst().city());
    }

    @Test
    void shouldGetActiveHotelDetails() {
        var service = service();
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(activeHotel(1L, "Danube Hotel", "Bratislava")));

        var result = service.getHotelDetails(1L);

        assertEquals("Danube Hotel", result.name());
        assertEquals(4, result.stars());
        assertEquals(true, result.childrenAllowed());
    }

    @Test
    void shouldRejectMissingHotelId() {
        var service = service();

        assertThrows(ValidationException.class, () -> service.getHotelDetails(null));
    }

    @Test
    void shouldHideInactiveHotelAsNotFound() {
        var service = service();
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(inactiveHotel(1L)));

        assertThrows(NotFoundException.class, () -> service.getHotelDetails(1L));
    }

    @Test
    void shouldListActiveHotelServices() {
        var service = service();
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(activeHotel(1L, "Danube Hotel", "Bratislava")));
        when(serviceOfferingRepository.findActiveByHotelId(1L)).thenReturn(List.of(
                serviceOffering(10L, 1L, "BREAKFAST")
        ));

        var result = service.listHotelServices(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().serviceOfferingId());
        assertEquals("BREAKFAST", result.getFirst().code());
        assertEquals("EUR", result.getFirst().price().currency().getCurrencyCode());
    }

    @Test
    void shouldListRoomTypesForActiveHotel() {
        var service = service();
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(activeHotel(1L, "Danube Hotel", "Bratislava")));
        when(roomTypeRepository.findByHotelIds(List.of(1L))).thenReturn(List.of(roomType(2L, 1L, "Standard")));

        var result = service.listRoomTypes(1L);

        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().roomTypeId());
        assertEquals("Standard", result.getFirst().name());
    }

    @Test
    void shouldHideRoomTypesOfInactiveHotelAsNotFound() {
        var service = service();
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(inactiveHotel(1L)));

        assertThrows(NotFoundException.class, () -> service.listRoomTypes(1L));
    }

    private HotelQueryService service() {
        return new HotelQueryService(
                hotelRepository,
                serviceOfferingRepository,
                roomTypeRepository,
                new HotelQueryResultMapper()
        );
    }

    private static RoomType roomType(Long id, Long hotelId, String name) {
        return new RoomType(
                id,
                hotelId,
                name,
                new OccupancyPolicy(2, 1, 1, 3),
                new PetPolicy(false, 0, Set.of(), null, null),
                Money.of("100.00", "EUR"),
                name + " room",
                RoomTypeFeatures.empty()
        );
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
                new HotelPolicy(true, true, 2, 12, 13)
        );
    }

    private static Hotel inactiveHotel(Long id) {
        return new Hotel(
                id,
                "Closed Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 2",
                4,
                "Closed hotel",
                HotelStatus.INACTIVE,
                new HotelPolicy(true, true, 2, 12, 13)
        );
    }

    private static ServiceOffering serviceOffering(Long id, Long hotelId, String code) {
        return new ServiceOffering(
                id,
                hotelId,
                code,
                "Breakfast",
                "Breakfast buffet",
                Money.of("12.00", "EUR"),
                true,
                "DAILY"
        );
    }
}
