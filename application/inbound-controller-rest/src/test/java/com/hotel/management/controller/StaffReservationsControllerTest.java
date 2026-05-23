package com.hotel.management.controller;

import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.api.dto.RoomOperationResponse;
import com.hotel.management.api.dto.UpdateRoomStatusRequest;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.mapper.RoomMapper;
import com.hotel.management.domain.service.reservation.CreatePublicReservationCommand;
import com.hotel.management.domain.service.reservation.CreateReservationCommand;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.service.reservation.CreateStaffReservationCommand;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.domain.service.room.RoomOperationsFacade;
import com.hotel.management.domain.service.room.UpdateRoomStatusCommand;
import com.hotel.management.domain.service.staff.StaffReservationFacade;
import com.hotel.management.domain.reservation.StaffReservationResult;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StaffReservationsControllerTest {

    private final TestStaffReservationFacade staffReservationFacade = new TestStaffReservationFacade();
    private final TestReservationFacade reservationFacade = new TestReservationFacade();
    private final TestRoomOperationsFacade roomOperationsFacade = new TestRoomOperationsFacade();
    private final TestCurrentUserPort currentUserPort = new TestCurrentUserPort();
    private final TestReservationMapper reservationMapper = new TestReservationMapper();
    private final TestRoomMapper roomMapper = new TestRoomMapper();
    private final StaffReservationsController controller = new StaffReservationsController(
            staffReservationFacade,
            reservationFacade,
            roomOperationsFacade,
            currentUserPort,
            reservationMapper,
            roomMapper
    );

    @Test
    void shouldCheckInReservation() {
        reservationMapper.response = new ReservationResponse().reservationId("reservation-1");

        var actual = controller.checkInReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.response);
        assertThat(staffReservationFacade.checkInReservationId).isEqualTo("reservation-1");
        assertThat(staffReservationFacade.checkInActor).isSameAs(currentUserPort.user);
        assertThat(reservationMapper.result).isSameAs(staffReservationFacade.checkInResult);
    }

    @Test
    void shouldCheckOutReservation() {
        reservationMapper.response = new ReservationResponse().reservationId("reservation-1");

        var actual = controller.checkOutReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.response);
        assertThat(staffReservationFacade.checkOutReservationId).isEqualTo("reservation-1");
        assertThat(staffReservationFacade.checkOutActor).isSameAs(currentUserPort.user);
        assertThat(reservationMapper.result).isSameAs(staffReservationFacade.checkOutResult);
    }

    @Test
    void shouldMarkNoShowReservation() {
        reservationMapper.response = new ReservationResponse().reservationId("reservation-1");

        var actual = controller.markNoShowReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.response);
        assertThat(staffReservationFacade.markNoShowReservationId).isEqualTo("reservation-1");
        assertThat(staffReservationFacade.markNoShowActor).isSameAs(currentUserPort.user);
        assertThat(reservationMapper.result).isSameAs(staffReservationFacade.markNoShowResult);
    }

    @Test
    void shouldUpdateRoomStatus() {
        var request = new UpdateRoomStatusRequest(UpdateRoomStatusRequest.StatusEnum.CLEANING);
        roomMapper.command = new UpdateRoomStatusCommand(10L, RoomStatus.CLEANING);
        roomMapper.response = new RoomOperationResponse().roomId(10L);

        var actual = controller.updateRoomStatus(10L, request);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(roomMapper.response);
        assertThat(roomMapper.roomId).isEqualTo(10L);
        assertThat(roomMapper.request).isSameAs(request);
        assertThat(roomOperationsFacade.command).isSameAs(roomMapper.command);
        assertThat(roomOperationsFacade.actor).isSameAs(currentUserPort.user);
        assertThat(roomMapper.result).isSameAs(roomOperationsFacade.result);
    }

    @Test
    void shouldListRooms() {
        roomMapper.roomListResponse = List.of(new RoomOperationResponse().roomId(10L));

        var actual = controller.listRooms(1L);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(roomMapper.roomListResponse);
        assertThat(roomOperationsFacade.listRoomsActor).isSameAs(currentUserPort.user);
        assertThat(roomOperationsFacade.listRoomsHotelId).isEqualTo(1L);
        assertThat(roomMapper.roomListResult).isSameAs(roomOperationsFacade.listRoomsResult);
    }

    private static final class TestStaffReservationFacade implements StaffReservationFacade {

        private final StaffReservationResult checkInResult = null;
        private final StaffReservationResult checkOutResult = null;
        private final StaffReservationResult markNoShowResult = null;
        private AuthenticatedUser checkInActor;
        private AuthenticatedUser checkOutActor;
        private AuthenticatedUser markNoShowActor;
        private String checkInReservationId;
        private String checkOutReservationId;
        private String markNoShowReservationId;

        @Override
        public StaffReservationResult checkIn(AuthenticatedUser actor, String reservationId) {
            this.checkInActor = actor;
            this.checkInReservationId = reservationId;
            return checkInResult;
        }

        @Override
        public StaffReservationResult checkOut(AuthenticatedUser actor, String reservationId) {
            this.checkOutActor = actor;
            this.checkOutReservationId = reservationId;
            return checkOutResult;
        }

        @Override
        public StaffReservationResult markNoShow(AuthenticatedUser actor, String reservationId) {
            this.markNoShowActor = actor;
            this.markNoShowReservationId = reservationId;
            return markNoShowResult;
        }
    }

    private static final class TestReservationFacade implements ReservationFacade {

        @Override
        public CreateReservationResult createReservation(AuthenticatedUser actor, CreateReservationCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CreateReservationResult createPublicReservation(CreatePublicReservationCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CreateReservationResult createStaffReservation(AuthenticatedUser actor, CreateStaffReservationCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<GetReservationResult> listReservations(AuthenticatedUser actor, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<GetReservationResult> listMyReservations(AuthenticatedUser actor, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GetReservationResult getReservation(AuthenticatedUser actor, String reservationId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancelReservation(AuthenticatedUser actor, String reservationId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestRoomOperationsFacade implements RoomOperationsFacade {

        private final RoomOperationResult result = new RoomOperationResult(
                10L,
                1L,
                "101",
                2L,
                3,
                "CLEANING"
        );
        private final List<RoomOperationResult> listRoomsResult = List.of();
        private UpdateRoomStatusCommand command;
        private AuthenticatedUser actor;
        private AuthenticatedUser listRoomsActor;
        private Long listRoomsHotelId;

        @Override
        public RoomOperationResult updateRoomStatus(AuthenticatedUser actor, UpdateRoomStatusCommand command) {
            this.actor = actor;
            this.command = command;
            return result;
        }

        @Override
        public List<RoomOperationResult> listRooms(AuthenticatedUser actor, Long hotelId) {
            this.listRoomsActor = actor;
            this.listRoomsHotelId = hotelId;
            return listRoomsResult;
        }
    }

    private static final class TestReservationMapper extends ReservationMapper {

        private StaffReservationResult result;
        private ReservationResponse response;

        @Override
        public ReservationResponse toResponse(StaffReservationResult result) {
            this.result = result;
            return response;
        }
    }

    private static final class TestRoomMapper extends RoomMapper {

        private Long roomId;
        private UpdateRoomStatusRequest request;
        private UpdateRoomStatusCommand command;
        private RoomOperationResult result;
        private RoomOperationResponse response;
        private List<RoomOperationResult> roomListResult;
        private List<RoomOperationResponse> roomListResponse = List.of();

        @Override
        public UpdateRoomStatusCommand toCommand(Long roomId, UpdateRoomStatusRequest request) {
            this.roomId = roomId;
            this.request = request;
            return command;
        }

        @Override
        public RoomOperationResponse toResponse(RoomOperationResult result) {
            this.result = result;
            return response;
        }

        @Override
        public List<RoomOperationResponse> toResponse(List<RoomOperationResult> results) {
            this.roomListResult = results;
            return roomListResponse;
        }
    }

    private static final class TestCurrentUserPort implements CurrentUserPort {

        private final AuthenticatedUser user = new AuthenticatedUser("staff-1", Set.of("STAFF"));

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
