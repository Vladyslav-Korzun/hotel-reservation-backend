package com.hotel.management.controller;

import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.api.dto.RoomOperationResponse;
import com.hotel.management.api.dto.UpdateRoomStatusRequest;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.mapper.RoomMapper;
import com.hotel.management.service.reservation.CreatePublicReservationCommand;
import com.hotel.management.service.reservation.CreateReservationCommand;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.service.reservation.CreateStaffReservationCommand;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.service.reservation.ReservationFacade;
import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.service.room.RoomOperationsFacade;
import com.hotel.management.service.room.UpdateRoomStatusCommand;
import com.hotel.management.service.staff.StaffReservationFacade;
import com.hotel.management.domain.reservation.StaffReservationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaffReservationsControllerTest {

    private final TestStaffReservationFacade staffReservationFacade = new TestStaffReservationFacade();
    private final TestReservationFacade reservationFacade = new TestReservationFacade();
    private final TestRoomOperationsFacade roomOperationsFacade = new TestRoomOperationsFacade();
    private final TestReservationMapper reservationMapper = new TestReservationMapper();
    private final TestRoomMapper roomMapper = new TestRoomMapper();
    private final StaffReservationsController controller = new StaffReservationsController(
            staffReservationFacade,
            reservationFacade,
            roomOperationsFacade,
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
        assertThat(reservationMapper.result).isSameAs(staffReservationFacade.checkInResult);
    }

    @Test
    void shouldCheckOutReservation() {
        reservationMapper.response = new ReservationResponse().reservationId("reservation-1");

        var actual = controller.checkOutReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.response);
        assertThat(staffReservationFacade.checkOutReservationId).isEqualTo("reservation-1");
        assertThat(reservationMapper.result).isSameAs(staffReservationFacade.checkOutResult);
    }

    @Test
    void shouldMarkNoShowReservation() {
        reservationMapper.response = new ReservationResponse().reservationId("reservation-1");

        var actual = controller.markNoShowReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.response);
        assertThat(staffReservationFacade.markNoShowReservationId).isEqualTo("reservation-1");
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
        assertThat(roomMapper.result).isSameAs(roomOperationsFacade.result);
    }

    private static final class TestStaffReservationFacade implements StaffReservationFacade {

        private final StaffReservationResult checkInResult = null;
        private final StaffReservationResult checkOutResult = null;
        private final StaffReservationResult markNoShowResult = null;
        private String checkInReservationId;
        private String checkOutReservationId;
        private String markNoShowReservationId;

        @Override
        public StaffReservationResult checkIn(String reservationId) {
            this.checkInReservationId = reservationId;
            return checkInResult;
        }

        @Override
        public StaffReservationResult checkOut(String reservationId) {
            this.checkOutReservationId = reservationId;
            return checkOutResult;
        }

        @Override
        public StaffReservationResult markNoShow(String reservationId) {
            this.markNoShowReservationId = reservationId;
            return markNoShowResult;
        }
    }

    private static final class TestReservationFacade implements ReservationFacade {

        @Override
        public CreateReservationResult createReservation(CreateReservationCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CreateReservationResult createPublicReservation(CreatePublicReservationCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CreateReservationResult createStaffReservation(CreateStaffReservationCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<GetReservationResult> listReservations(int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<GetReservationResult> listMyReservations(int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GetReservationResult getReservation(String reservationId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancelReservation(String reservationId) {
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
        private UpdateRoomStatusCommand command;

        @Override
        public RoomOperationResult updateRoomStatus(UpdateRoomStatusCommand command) {
            this.command = command;
            return result;
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
    }
}