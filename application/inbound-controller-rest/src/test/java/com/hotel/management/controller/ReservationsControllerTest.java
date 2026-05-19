package com.hotel.management.controller;

import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.service.reservation.CreatePublicReservationCommand;
import com.hotel.management.service.reservation.CreateReservationCommand;
import com.hotel.management.service.reservation.CreateReservationResult;
import com.hotel.management.service.reservation.CreateStaffReservationCommand;
import com.hotel.management.service.reservation.GetReservationResult;
import com.hotel.management.service.reservation.ReservationFacade;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationsControllerTest {

    private final TestReservationFacade reservationFacade = new TestReservationFacade();
    private final TestReservationMapper reservationMapper = new TestReservationMapper();
    private final ReservationsController controller = new ReservationsController(
            reservationFacade,
            reservationMapper
    );

    @Test
    void shouldListReservationsWithDefaultLimit() {
        reservationMapper.listResponse = List.of(new ReservationResponse().reservationId("reservation-1"));

        var actual = controller.listReservations(null);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.listResponse);
        assertThat(reservationFacade.listReservationsLimit).isEqualTo(100);
        assertThat(reservationMapper.listResult).isSameAs(reservationFacade.listReservationsResult);
    }

    @Test
    void shouldGetReservation() {
        reservationMapper.singleResponse = new ReservationResponse().reservationId("reservation-1");

        var actual = controller.getReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.singleResponse);
        assertThat(reservationFacade.getReservationId).isEqualTo("reservation-1");
        assertThat(reservationMapper.singleResult).isSameAs(reservationFacade.getReservationResult);
    }

    @Test
    void shouldCancelReservation() {
        var actual = controller.cancelReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(204);
        assertThat(actual.getBody()).isNull();
        assertThat(reservationFacade.cancelReservationId).isEqualTo("reservation-1");
    }

    private static final class TestReservationFacade implements ReservationFacade {

        private final List<GetReservationResult> listReservationsResult = List.of();
        private final GetReservationResult getReservationResult = null;
        private int listReservationsLimit;
        private String getReservationId;
        private String cancelReservationId;

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
            this.listReservationsLimit = limit;
            return listReservationsResult;
        }

        @Override
        public List<GetReservationResult> listMyReservations(int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GetReservationResult getReservation(String reservationId) {
            this.getReservationId = reservationId;
            return getReservationResult;
        }

        @Override
        public void cancelReservation(String reservationId) {
            this.cancelReservationId = reservationId;
        }
    }

    private static final class TestReservationMapper extends ReservationMapper {

        private List<GetReservationResult> listResult;
        private GetReservationResult singleResult;
        private List<ReservationResponse> listResponse = List.of();
        private ReservationResponse singleResponse;

        @Override
        public List<ReservationResponse> toResponse(List<GetReservationResult> result) {
            this.listResult = result;
            return listResponse;
        }

        @Override
        public ReservationResponse toResponse(GetReservationResult result) {
            this.singleResult = result;
            return singleResponse;
        }
    }
}
