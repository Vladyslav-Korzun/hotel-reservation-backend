package com.hotel.management.controller;

import com.hotel.management.api.dto.HotelResponse;
import com.hotel.management.api.dto.HotelServiceOfferingResponse;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.mapper.AvailabilityMapper;
import com.hotel.management.mapper.HotelMapper;
import com.hotel.management.service.availability.GetRoomTypeAvailabilityCalendarFacade;
import com.hotel.management.service.availability.GetRoomTypeAvailabilityCalendarQuery;
import com.hotel.management.service.availability.RoomTypeAvailabilityCalendarDayResult;
import com.hotel.management.service.hotel.HotelQueryFacade;
import com.hotel.management.service.hotel.HotelResult;
import com.hotel.management.service.hotel.HotelServiceOfferingResult;
import com.hotel.management.service.hotel.ListHotelsQuery;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotelsControllerTest {

    private final TestHotelQueryFacade hotelQueryFacade = new TestHotelQueryFacade();
    private final TestAvailabilityCalendarFacade availabilityCalendarFacade = new TestAvailabilityCalendarFacade();
    private final TestHotelMapper hotelMapper = new TestHotelMapper();
    private final HotelsController controller = new HotelsController(
            hotelQueryFacade,
            availabilityCalendarFacade,
            hotelMapper,
            new AvailabilityMapper()
    );

    @Test
    void shouldListHotels() {
        hotelMapper.hotelResponses = List.of(new HotelResponse().hotelId(1L));

        var actual = controller.listHotels("Bratislava");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(hotelMapper.hotelResponses);
        assertThat(hotelMapper.city).isEqualTo("Bratislava");
        assertThat(hotelQueryFacade.listHotelsQuery).isSameAs(hotelMapper.query);
        assertThat(hotelMapper.hotelResults).isSameAs(hotelQueryFacade.listHotelsResult);
    }

    @Test
    void shouldGetHotelDetails() {
        hotelMapper.hotelResponse = new HotelResponse().hotelId(1L);

        var actual = controller.getHotelDetails(1L);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(hotelMapper.hotelResponse);
        assertThat(hotelQueryFacade.hotelDetailsId).isEqualTo(1L);
        assertThat(hotelMapper.hotelResult).isSameAs(hotelQueryFacade.hotelDetailsResult);
    }

    @Test
    void shouldListHotelServices() {
        hotelMapper.serviceOfferingResponses = List.of(new HotelServiceOfferingResponse().serviceOfferingId(10L));

        var actual = controller.listHotelServices(1L);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(hotelMapper.serviceOfferingResponses);
        assertThat(hotelQueryFacade.hotelServicesId).isEqualTo(1L);
        assertThat(hotelMapper.serviceOfferingResults).isSameAs(hotelQueryFacade.hotelServicesResult);
    }

    @Test
    void shouldGetRoomTypeAvailabilityCalendar() {
        var actual = controller.getRoomTypeAvailabilityCalendar(
                1L,
                10L,
                LocalDate.parse("2026-06-01"),
                LocalDate.parse("2026-06-02")
        );

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).hasSize(1);
        assertThat(actual.getBody().getFirst().getDate()).isEqualTo(LocalDate.parse("2026-06-01"));
        assertThat(actual.getBody().getFirst().getAvailableCount()).isEqualTo(1);
        assertThat(actual.getBody().getFirst().getAvailable()).isTrue();
        assertThat(availabilityCalendarFacade.query.hotelId()).isEqualTo(1L);
        assertThat(availabilityCalendarFacade.query.roomTypeId()).isEqualTo(10L);
        assertThat(availabilityCalendarFacade.query.from()).isEqualTo(LocalDate.parse("2026-06-01"));
        assertThat(availabilityCalendarFacade.query.to()).isEqualTo(LocalDate.parse("2026-06-02"));
    }

    private static final class TestHotelQueryFacade implements HotelQueryFacade {

        private final List<HotelResult> listHotelsResult = List.of(hotelResult());
        private final HotelResult hotelDetailsResult = hotelResult();
        private final List<HotelServiceOfferingResult> hotelServicesResult = List.of(new HotelServiceOfferingResult(
                10L,
                1L,
                "SPA",
                "Spa",
                "Wellness access",
                Money.of("30.00", "EUR"),
                true,
                "DAILY"
        ));
        private ListHotelsQuery listHotelsQuery;
        private Long hotelDetailsId;
        private Long hotelServicesId;

        @Override
        public List<HotelResult> listHotels(ListHotelsQuery query) {
            this.listHotelsQuery = query;
            return listHotelsResult;
        }

        @Override
        public HotelResult getHotelDetails(Long hotelId) {
            this.hotelDetailsId = hotelId;
            return hotelDetailsResult;
        }

        @Override
        public List<HotelServiceOfferingResult> listHotelServices(Long hotelId) {
            this.hotelServicesId = hotelId;
            return hotelServicesResult;
        }
    }

    private static final class TestAvailabilityCalendarFacade implements GetRoomTypeAvailabilityCalendarFacade {

        private GetRoomTypeAvailabilityCalendarQuery query;

        @Override
        public List<RoomTypeAvailabilityCalendarDayResult> getAvailabilityCalendar(GetRoomTypeAvailabilityCalendarQuery query) {
            this.query = query;
            return List.of(new RoomTypeAvailabilityCalendarDayResult(
                    LocalDate.parse("2026-06-01"),
                    1,
                    true
            ));
        }
    }

    private static final class TestHotelMapper extends HotelMapper {

        private final ListHotelsQuery query = new ListHotelsQuery("Bratislava");
        private String city;
        private List<HotelResult> hotelResults;
        private HotelResult hotelResult;
        private List<HotelServiceOfferingResult> serviceOfferingResults;
        private List<HotelResponse> hotelResponses = List.of();
        private HotelResponse hotelResponse;
        private List<HotelServiceOfferingResponse> serviceOfferingResponses = List.of();

        @Override
        public ListHotelsQuery toQuery(String city) {
            this.city = city;
            return query;
        }

        @Override
        public List<HotelResponse> toHotelResponse(List<HotelResult> results) {
            this.hotelResults = results;
            return hotelResponses;
        }

        @Override
        public HotelResponse toResponse(HotelResult result) {
            this.hotelResult = result;
            return hotelResponse;
        }

        @Override
        public List<HotelServiceOfferingResponse> toServiceOfferingResponse(List<HotelServiceOfferingResult> results) {
            this.serviceOfferingResults = results;
            return serviceOfferingResponses;
        }
    }

    private static HotelResult hotelResult() {
        return new HotelResult(
                1L,
                "Hotel Danube",
                "Bratislava",
                "Slovakia",
                "River 1",
                4,
                "City hotel",
                "ACTIVE",
                true,
                true,
                2,
                12,
                13
        );
    }
}
