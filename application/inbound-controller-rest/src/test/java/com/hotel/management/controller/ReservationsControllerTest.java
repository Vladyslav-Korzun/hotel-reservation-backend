package com.hotel.management.controller;

import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.mapper.ReservationMapper;
import com.hotel.management.domain.service.reservation.CreatePublicReservationCommand;
import com.hotel.management.domain.service.reservation.CreateReservationCommand;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.service.reservation.CreateStaffReservationCommand;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.domain.service.reservation.ReservationFacade;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationsControllerTest {

    private final TestReservationFacade reservationFacade = new TestReservationFacade();
    private final TestCurrentUserPort currentUserPort = new TestCurrentUserPort();
    private final TestReservationMapper reservationMapper = new TestReservationMapper();
    private final ReservationsController controller = new ReservationsController(
            reservationFacade,
            currentUserPort,
            reservationMapper
    );

    @Test
    void shouldListReservationsWithDefaultLimit() {
        reservationMapper.listResponse = List.of(new ReservationResponse().reservationId("reservation-1"));

        var actual = controller.listReservations(null);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.listResponse);
        assertThat(reservationFacade.listReservationsActor).isSameAs(currentUserPort.user);
        assertThat(reservationFacade.listReservationsLimit).isEqualTo(100);
        assertThat(reservationMapper.listResult).isSameAs(reservationFacade.listReservationsResult);
    }

    @Test
    void shouldGetReservation() {
        reservationMapper.singleResponse = new ReservationResponse().reservationId("reservation-1");

        var actual = controller.getReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.singleResponse);
        assertThat(reservationFacade.getReservationActor).isSameAs(currentUserPort.user);
        assertThat(reservationFacade.getReservationId).isEqualTo("reservation-1");
        assertThat(reservationMapper.singleResult).isSameAs(reservationFacade.getReservationResult);
    }

    @Test
    void shouldCancelReservation() {
        var actual = controller.cancelReservation("reservation-1");

        assertThat(actual.getStatusCode().value()).isEqualTo(204);
        assertThat(actual.getBody()).isNull();
        assertThat(reservationFacade.cancelReservationActor).isSameAs(currentUserPort.user);
        assertThat(reservationFacade.cancelReservationId).isEqualTo("reservation-1");
    }

    private static final class TestReservationFacade implements ReservationFacade {

        private final List<GetReservationResult> listReservationsResult = List.of();
        private final GetReservationResult getReservationResult = null;
        private AuthenticatedUser listReservationsActor;
        private AuthenticatedUser getReservationActor;
        private AuthenticatedUser cancelReservationActor;
        private int listReservationsLimit;
        private String getReservationId;
        private String cancelReservationId;

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
            this.listReservationsActor = actor;
            this.listReservationsLimit = limit;
            return listReservationsResult;
        }

        @Override
        public List<GetReservationResult> listMyReservations(AuthenticatedUser actor, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GetReservationResult getReservation(AuthenticatedUser actor, String reservationId) {
            this.getReservationActor = actor;
            this.getReservationId = reservationId;
            return getReservationResult;
        }

        @Override
        public void cancelReservation(AuthenticatedUser actor, String reservationId) {
            this.cancelReservationActor = actor;
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

    private static final class TestCurrentUserPort implements CurrentUserPort {

        private final AuthenticatedUser user = new AuthenticatedUser("staff-1", Set.of("STAFF"));

        @Override
        public AuthenticatedUser getCurrentUser() {
            return user;
        }
    }
}
