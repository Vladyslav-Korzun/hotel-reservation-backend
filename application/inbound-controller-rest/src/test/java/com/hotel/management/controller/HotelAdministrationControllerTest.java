package com.hotel.management.controller;

import com.hotel.management.api.dto.CreateHotelRequest;
import com.hotel.management.api.dto.HotelResponse;
import com.hotel.management.api.dto.UpdateHotelRequest;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.mapper.HotelMapper;
import com.hotel.management.service.hotel.CreateHotelCommand;
import com.hotel.management.service.hotel.CreateRoomCommand;
import com.hotel.management.service.hotel.CreateRoomTypeCommand;
import com.hotel.management.service.hotel.CreateServiceOfferingCommand;
import com.hotel.management.service.hotel.DeactivateServiceOfferingCommand;
import com.hotel.management.service.hotel.HotelAdministrationFacade;
import com.hotel.management.service.hotel.HotelResult;
import com.hotel.management.service.hotel.HotelServiceOfferingResult;
import com.hotel.management.service.hotel.RoomResult;
import com.hotel.management.service.hotel.RoomTypeResult;
import com.hotel.management.service.hotel.UpdateHotelCommand;
import com.hotel.management.service.hotel.UpdateRoomCommand;
import com.hotel.management.service.hotel.UpdateRoomTypeCommand;
import com.hotel.management.service.hotel.UpdateServiceOfferingCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HotelAdministrationControllerTest {

    private final TestHotelAdministrationFacade hotelAdministrationFacade = new TestHotelAdministrationFacade();
    private final TestHotelMapper hotelMapper = new TestHotelMapper();
    private final HotelAdministrationController controller = new HotelAdministrationController(
            hotelAdministrationFacade,
            hotelMapper
    );

    @Test
    void shouldCreateHotel() {
        var request = new CreateHotelRequest();
        hotelMapper.createHotelCommand = createHotelCommand();
        hotelMapper.hotelResponse = new HotelResponse().hotelId(1L);

        var actual = controller.createHotel(request);

        assertThat(actual.getStatusCode().value()).isEqualTo(201);
        assertThat(actual.getBody()).isSameAs(hotelMapper.hotelResponse);
        assertThat(hotelMapper.createHotelRequest).isSameAs(request);
        assertThat(hotelAdministrationFacade.createHotelCommand).isSameAs(hotelMapper.createHotelCommand);
        assertThat(hotelMapper.hotelResult).isSameAs(hotelAdministrationFacade.hotelResult);
    }

    @Test
    void shouldUpdateHotel() {
        var request = new UpdateHotelRequest();
        hotelMapper.updateHotelCommand = updateHotelCommand();
        hotelMapper.hotelResponse = new HotelResponse().hotelId(1L);

        var actual = controller.updateHotel(1L, request);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(hotelMapper.hotelResponse);
        assertThat(hotelMapper.updateHotelId).isEqualTo(1L);
        assertThat(hotelMapper.updateHotelRequest).isSameAs(request);
        assertThat(hotelAdministrationFacade.updateHotelCommand).isSameAs(hotelMapper.updateHotelCommand);
    }

    private static final class TestHotelAdministrationFacade implements HotelAdministrationFacade {

        private final HotelResult hotelResult = hotelResult();
        private CreateHotelCommand createHotelCommand;
        private UpdateHotelCommand updateHotelCommand;

        @Override
        public HotelResult createHotel(CreateHotelCommand command) {
            this.createHotelCommand = command;
            return hotelResult;
        }

        @Override
        public HotelResult updateHotel(UpdateHotelCommand command) {
            this.updateHotelCommand = command;
            return hotelResult;
        }

        @Override
        public RoomTypeResult createRoomType(CreateRoomTypeCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RoomTypeResult updateRoomType(UpdateRoomTypeCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RoomResult createRoom(CreateRoomCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RoomResult updateRoom(UpdateRoomCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HotelServiceOfferingResult createServiceOffering(CreateServiceOfferingCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HotelServiceOfferingResult updateServiceOffering(UpdateServiceOfferingCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deactivateServiceOffering(DeactivateServiceOfferingCommand command) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestHotelMapper extends HotelMapper {

        private CreateHotelRequest createHotelRequest;
        private CreateHotelCommand createHotelCommand;
        private Long updateHotelId;
        private UpdateHotelRequest updateHotelRequest;
        private UpdateHotelCommand updateHotelCommand;
        private HotelResult hotelResult;
        private HotelResponse hotelResponse;

        @Override
        public CreateHotelCommand toCommand(CreateHotelRequest request) {
            this.createHotelRequest = request;
            return createHotelCommand;
        }

        @Override
        public UpdateHotelCommand toCommand(Long hotelId, UpdateHotelRequest request) {
            this.updateHotelId = hotelId;
            this.updateHotelRequest = request;
            return updateHotelCommand;
        }

        @Override
        public HotelResponse toResponse(HotelResult result) {
            this.hotelResult = result;
            return hotelResponse;
        }
    }

    private static CreateHotelCommand createHotelCommand() {
        return new CreateHotelCommand(
                1L,
                "Hotel Danube",
                "Bratislava",
                "Slovakia",
                "River 1",
                4,
                "City hotel",
                HotelStatus.ACTIVE,
                true,
                true,
                2,
                12,
                13
        );
    }

    private static UpdateHotelCommand updateHotelCommand() {
        return new UpdateHotelCommand(
                1L,
                "Hotel Danube Updated",
                "Bratislava",
                "Slovakia",
                "River 1",
                5,
                "Updated hotel",
                HotelStatus.ACTIVE,
                true,
                false,
                2,
                12,
                13
        );
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
