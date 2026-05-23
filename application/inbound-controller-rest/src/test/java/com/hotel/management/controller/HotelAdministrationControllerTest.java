package com.hotel.management.controller;

import com.hotel.management.api.dto.CreateHotelRequest;
import com.hotel.management.api.dto.HotelResponse;
import com.hotel.management.api.dto.UpdateHotelRequest;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.mapper.HotelMapper;
import com.hotel.management.domain.service.hotel.CreateHotelCommand;
import com.hotel.management.domain.service.hotel.CreateRoomCommand;
import com.hotel.management.domain.service.hotel.CreateRoomTypeCommand;
import com.hotel.management.domain.service.hotel.CreateServiceOfferingCommand;
import com.hotel.management.domain.service.hotel.DeactivateServiceOfferingCommand;
import com.hotel.management.domain.service.hotel.HotelFacade;
import com.hotel.management.domain.service.hotel.RoomAdministrationFacade;
import com.hotel.management.domain.service.hotel.RoomTypeFacade;
import com.hotel.management.domain.service.hotel.ServiceOfferingFacade;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.service.hotel.UpdateHotelCommand;
import com.hotel.management.domain.service.hotel.UpdateRoomCommand;
import com.hotel.management.domain.service.hotel.UpdateRoomTypeCommand;
import com.hotel.management.domain.service.hotel.UpdateServiceOfferingCommand;
import com.hotel.management.api.dto.AssignStaffToHotelRequest;
import com.hotel.management.api.dto.StaffResponse;
import com.hotel.management.domain.service.staff.AssignStaffToHotelCommand;
import com.hotel.management.domain.service.staff.StaffFacade;
import com.hotel.management.domain.staff.Staff;
import com.hotel.management.domain.staff.StaffResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HotelAdministrationControllerTest {

    private final TestHotelFacade hotelFacade = new TestHotelFacade();
    private final TestCurrentUserPort currentUserPort = new TestCurrentUserPort();
    private final TestHotelMapper hotelMapper = new TestHotelMapper();
    private final TestStaffFacade staffFacade = new TestStaffFacade();
    private final HotelAdministrationController controller = new HotelAdministrationController(
            hotelFacade,
            new TestRoomTypeFacade(),
            new TestRoomAdministrationFacade(),
            new TestServiceOfferingFacade(),
            staffFacade,
            currentUserPort,
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
        assertThat(hotelFacade.createHotelActor).isSameAs(currentUserPort.user);
        assertThat(hotelFacade.createHotelCommand).isSameAs(hotelMapper.createHotelCommand);
        assertThat(hotelMapper.hotelResult).isSameAs(hotelFacade.hotelResult);
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
        assertThat(hotelFacade.updateHotelActor).isSameAs(currentUserPort.user);
        assertThat(hotelFacade.updateHotelCommand).isSameAs(hotelMapper.updateHotelCommand);
    }

    @Test
    void shouldListStaff() {
        hotelMapper.staffResponseList = List.of(new StaffResponse().id(1L));

        var actual = controller.listStaff();

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(hotelMapper.staffResponseList);
        assertThat(staffFacade.listStaffActor).isSameAs(currentUserPort.user);
        assertThat(hotelMapper.staffResultList).isSameAs(staffFacade.staffList);
    }

    @Test
    void shouldAssignStaffToHotel() {
        var request = new AssignStaffToHotelRequest();
        hotelMapper.assignStaffCommand = new AssignStaffToHotelCommand(1L, 7L);
        hotelMapper.staffResponse = new StaffResponse().id(1L);

        var actual = controller.assignStaffToHotel(1L, request);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(hotelMapper.staffResponse);
        assertThat(hotelMapper.assignStaffId).isEqualTo(1L);
        assertThat(hotelMapper.assignStaffRequest).isSameAs(request);
        assertThat(staffFacade.assignActor).isSameAs(currentUserPort.user);
        assertThat(staffFacade.assignCommand).isSameAs(hotelMapper.assignStaffCommand);
        assertThat(hotelMapper.staffResult).isSameAs(staffFacade.assignResult);
    }

    @Test
    void shouldUnassignStaffFromHotel() {
        hotelMapper.staffResponse = new StaffResponse().id(1L);

        var actual = controller.unassignStaffFromHotel(1L);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(hotelMapper.staffResponse);
        assertThat(staffFacade.unassignActor).isSameAs(currentUserPort.user);
        assertThat(staffFacade.unassignStaffId).isEqualTo(1L);
        assertThat(hotelMapper.staffResult).isSameAs(staffFacade.unassignResult);
    }

    private static final class TestHotelFacade implements HotelFacade {

        private final HotelResult hotelResult = hotelResult();
        private AuthenticatedUser createHotelActor;
        private AuthenticatedUser updateHotelActor;
        private CreateHotelCommand createHotelCommand;
        private UpdateHotelCommand updateHotelCommand;

        @Override
        public HotelResult createHotel(AuthenticatedUser actor, CreateHotelCommand command) {
            this.createHotelActor = actor;
            this.createHotelCommand = command;
            return hotelResult;
        }

        @Override
        public HotelResult updateHotel(AuthenticatedUser actor, UpdateHotelCommand command) {
            this.updateHotelActor = actor;
            this.updateHotelCommand = command;
            return hotelResult;
        }

    }

    private static final class TestRoomTypeFacade implements RoomTypeFacade {

        @Override
        public RoomTypeResult createRoomType(AuthenticatedUser actor, CreateRoomTypeCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RoomTypeResult updateRoomType(AuthenticatedUser actor, UpdateRoomTypeCommand command) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestRoomAdministrationFacade implements RoomAdministrationFacade {

        @Override
        public RoomResult createRoom(AuthenticatedUser actor, CreateRoomCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RoomResult updateRoom(AuthenticatedUser actor, UpdateRoomCommand command) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestServiceOfferingFacade implements ServiceOfferingFacade {

        @Override
        public HotelServiceOfferingResult createServiceOffering(AuthenticatedUser actor, CreateServiceOfferingCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HotelServiceOfferingResult updateServiceOffering(AuthenticatedUser actor, UpdateServiceOfferingCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deactivateServiceOffering(AuthenticatedUser actor, DeactivateServiceOfferingCommand command) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestStaffFacade implements StaffFacade {

        private final List<StaffResult> staffList = List.of(new StaffResult(1L, "sub-1", 5L));
        private final StaffResult assignResult = new StaffResult(1L, "sub-1", 7L);
        private final StaffResult unassignResult = new StaffResult(1L, "sub-1", null);
        private AuthenticatedUser listStaffActor;
        private AuthenticatedUser assignActor;
        private AssignStaffToHotelCommand assignCommand;
        private AuthenticatedUser unassignActor;
        private Long unassignStaffId;

        @Override
        public List<StaffResult> listStaff(AuthenticatedUser actor) {
            this.listStaffActor = actor;
            return staffList;
        }

        @Override
        public StaffResult assignStaffToHotel(AuthenticatedUser actor, AssignStaffToHotelCommand command) {
            this.assignActor = actor;
            this.assignCommand = command;
            return assignResult;
        }

        @Override
        public StaffResult unassignStaffFromHotel(AuthenticatedUser actor, Long staffId) {
            this.unassignActor = actor;
            this.unassignStaffId = staffId;
            return unassignResult;
        }

        @Override
        public Staff resolveStaff(AuthenticatedUser actor) {
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

        private List<StaffResult> staffResultList;
        private List<StaffResponse> staffResponseList = List.of();
        private Long assignStaffId;
        private AssignStaffToHotelRequest assignStaffRequest;
        private AssignStaffToHotelCommand assignStaffCommand;
        private StaffResult staffResult;
        private StaffResponse staffResponse;

        @Override
        public List<StaffResponse> toStaffResponseList(List<StaffResult> results) {
            this.staffResultList = results;
            return staffResponseList;
        }

        @Override
        public AssignStaffToHotelCommand toAssignStaffCommand(Long staffId, AssignStaffToHotelRequest request) {
            this.assignStaffId = staffId;
            this.assignStaffRequest = request;
            return assignStaffCommand;
        }

        @Override
        public StaffResponse toStaffResponse(StaffResult result) {
            this.staffResult = result;
            return staffResponse;
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

    private static final class TestCurrentUserPort implements CurrentUserPort {

        private final AuthenticatedUser user = new AuthenticatedUser("admin-1", Set.of("ADMIN"));

        @Override
        public AuthenticatedUser getCurrentUser() {
            return user;
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }
    }
}
