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

class MeReservationsControllerTest {

    private final TestReservationFacade reservationFacade = new TestReservationFacade();
    private final TestCurrentUserPort currentUserPort = new TestCurrentUserPort();
    private final TestReservationMapper reservationMapper = new TestReservationMapper();
    private final MeReservationsController controller = new MeReservationsController(
            reservationFacade,
            currentUserPort,
            reservationMapper
    );

    @Test
    void shouldListCurrentUserReservationsWithDefaultLimit() {
        reservationMapper.response = List.of(new ReservationResponse().reservationId("reservation-1"));

        var actual = controller.listMyReservations(null);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isSameAs(reservationMapper.response);
        assertThat(reservationFacade.listMyReservationsActor).isSameAs(currentUserPort.user);
        assertThat(reservationFacade.listMyReservationsLimit).isEqualTo(100);
        assertThat(reservationMapper.listResult).isSameAs(reservationFacade.listMyReservationsResult);
    }

    @Test
    void shouldListCurrentUserReservationsWithRequestedLimit() {
        var actual = controller.listMyReservations(25);

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(reservationFacade.listMyReservationsActor).isSameAs(currentUserPort.user);
        assertThat(reservationFacade.listMyReservationsLimit).isEqualTo(25);
    }

    private static final class TestReservationFacade implements ReservationFacade {

        private final List<GetReservationResult> listMyReservationsResult = List.of();
        private AuthenticatedUser listMyReservationsActor;
        private int listMyReservationsLimit;

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
            this.listMyReservationsActor = actor;
            this.listMyReservationsLimit = limit;
            return listMyReservationsResult;
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

    private static final class TestReservationMapper extends ReservationMapper {

        private List<GetReservationResult> listResult;
        private List<ReservationResponse> response = List.of();

        @Override
        public List<ReservationResponse> toResponse(List<GetReservationResult> result) {
            this.listResult = result;
            return response;
        }
    }

    private static final class TestCurrentUserPort implements CurrentUserPort {

        private final AuthenticatedUser user = new AuthenticatedUser("guest-1", Set.of("GUEST"));

        @Override
        public AuthenticatedUser getCurrentUser() {
            return user;
        }
    }
}
