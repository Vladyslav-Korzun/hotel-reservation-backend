package com.hotel.management.service.reservation;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.service.exception.ForbiddenException;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.security.AuthenticatedUser;
import com.hotel.management.service.security.CurrentUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private ReservationQueryPort reservationQueryPort;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ReservationLockPort reservationLockPort;

    @Mock
    private RoomInventoryPort roomInventoryPort;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private ClockPort clockPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private AccommodationPolicyValidator accommodationPolicyValidator;

    @InjectMocks
    private ReservationService facade;

    @Test
    void shouldCreateReservationForAuthenticatedUser() {
        var periodCheckIn = LocalDate.parse("2026-05-10");
        var periodCheckOut = LocalDate.parse("2026-05-12");
        stubAvailableRoomType(1L, 2L);
        when(reservationQueryPort.findActiveOverlapping(any(), any())).thenReturn(List.of());
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                periodCheckIn,
                periodCheckOut,
                party(2)
        ));

        assertEquals("PENDING", result.status());
        assertEquals(1L, result.hotelId());
        assertEquals("guest-123", result.createdBy());
        assertEquals(2, result.adults());
        assertEquals(Instant.parse("2026-04-03T12:00:00Z"), result.createdAt());
    }

    @Test
    void shouldRejectInvalidStayPeriod() {
        assertThrows(ValidationException.class, () -> facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-10"),
                party(2)
        )));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectReservationWhenAllRoomsAreBookedForPeriod() {
        stubAvailableRoomType(1L, 2L);
        when(reservationQueryPort.findActiveOverlapping(any(), any())).thenReturn(List.of(new ActiveReservationView(1L, 2L)));
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));

        assertThrows(ValidationException.class, () -> facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2)
        )));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectReservationInPast() {
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));

        assertThrows(ValidationException.class, () -> facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-04-02"),
                LocalDate.parse("2026-04-04"),
                party(2)
        )));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnReservationById() {
        Reservation reservation = pendingReservation("reservation-1", 1L, null, 2L, "guest-123");
        when(repository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        var result = facade.getReservation("reservation-1");

        assertEquals("reservation-1", result.reservationId());
        assertEquals(1L, result.hotelId());
        assertEquals(2, result.adults());
    }

    @Test
    void shouldAllowStaffToListAllReservations() {
        Reservation firstReservation = pendingReservation("reservation-1", 1L, null, 2L, "guest-123");
        Reservation secondReservation = Reservation.rehydrate(
                "reservation-2",
                3L,
                4L,
                LocalDate.parse("2026-06-10"),
                LocalDate.parse("2026-06-12"),
                party(1),
                ReservationStatus.CANCELLED,
                Instant.parse("2026-04-04T12:00:00Z"),
                Instant.parse("2026-04-05T12:00:00Z"),
                "guest-456"
        );
        when(repository.findAll(100)).thenReturn(List.of(firstReservation, secondReservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-100", Set.of("STAFF")));

        var result = facade.listReservations(100);

        assertEquals(2, result.size());
        assertEquals("reservation-1", result.getFirst().reservationId());
        assertTrue(result.stream().anyMatch(item -> item.createdBy().equals("guest-456")));
    }

    @Test
    void shouldRejectGuestListingAllReservations() {
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ForbiddenException.class, () -> facade.listReservations(100));
        verify(repository, never()).findAll(100);
    }

    @Test
    void shouldThrowWhenReservationNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> facade.getReservation("missing"));
    }

    @Test
    void shouldCancelReservationWhenFound() {
        Reservation reservation = pendingReservation("reservation-1", 1L, null, 2L, "guest-123");
        when(reservationLockPort.findReservationForChange("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-04T10:00:00Z"));
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        facade.cancelReservation("reservation-1");

        verify(repository).save(any(Reservation.class));
    }

    @Test
    void shouldThrowWhenCancellingMissingReservation() {
        when(reservationLockPort.findReservationForChange("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> facade.cancelReservation("missing"));
    }

    @Test
    void shouldRejectAccessToReservationOwnedByAnotherUser() {
        Reservation reservation = pendingReservation("reservation-1", 1L, null, 2L, "guest-123");
        when(repository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-999", Set.of("GUEST")));

        assertThrows(ForbiddenException.class, () -> facade.getReservation("reservation-1"));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldAllowStaffToReadReservationOwnedByAnotherUser() {
        Reservation reservation = pendingReservation("reservation-1", 1L, null, 2L, "guest-123");
        when(repository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-100", Set.of("STAFF")));

        var result = facade.getReservation("reservation-1");

        assertEquals("reservation-1", result.reservationId());
        assertEquals("guest-123", result.createdBy());
    }

    @Test
    void shouldRejectCancellingAlreadyCancelledReservation() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                ReservationStatus.CANCELLED,
                Instant.parse("2026-04-03T12:00:00Z"),
                Instant.parse("2026-04-04T10:00:00Z"),
                "guest-123"
        );
        when(reservationLockPort.findReservationForChange("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ValidationException.class, () -> facade.cancelReservation("reservation-1"));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectCancellingCheckedInReservation() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                100L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                ReservationStatus.CHECKED_IN,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        );
        when(reservationLockPort.findReservationForChange("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ValidationException.class, () -> facade.cancelReservation("reservation-1"));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectCancellingCheckedOutReservation() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                100L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                ReservationStatus.CHECKED_OUT,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        );
        when(reservationLockPort.findReservationForChange("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ValidationException.class, () -> facade.cancelReservation("reservation-1"));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectCancellingNoShowReservation() {
        Reservation reservation = Reservation.rehydrate(
                "reservation-1",
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                ReservationStatus.NO_SHOW,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        );
        when(reservationLockPort.findReservationForChange("reservation-1")).thenReturn(Optional.of(reservation));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ValidationException.class, () -> facade.cancelReservation("reservation-1"));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectCheckedInReservationWithoutAssignedRoom() {
        assertThrows(ValidationException.class, () -> Reservation.rehydrate(
                "reservation-1",
                1L,
                null,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                ReservationStatus.CHECKED_IN,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        ));
    }

    @Test
    void shouldRejectCheckedOutReservationWithoutAssignedRoom() {
        assertThrows(ValidationException.class, () -> Reservation.rehydrate(
                "reservation-1",
                1L,
                null,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                ReservationStatus.CHECKED_OUT,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                "guest-123"
        ));
    }

    private void stubAvailableRoomType(Long hotelId, Long roomTypeId) {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(new Hotel(
                hotelId,
                "Danube Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                HotelStatus.ACTIVE,
                new HotelPolicy(true, true, 2, 11)
        )));
        when(roomTypeRepository.findByHotelIds(List.of(hotelId))).thenReturn(List.of(new RoomType(
                roomTypeId,
                hotelId,
                "Standard",
                new OccupancyPolicy(2, 1, 1, 3),
                new PetPolicy(false, 0, Set.of(), null, null),
                Money.of("100.00", "EUR"),
                "Standard room"
        )));
        when(roomInventoryPort.countBookableRoomsForReservation(hotelId, roomTypeId)).thenReturn(1L);
    }

    private static AccommodationParty party(int adults, Integer... childrenAges) {
        return new AccommodationParty(new GuestComposition(adults, List.of(childrenAges)), List.of());
    }

    private static Reservation pendingReservation(String reservationId, Long hotelId, Long roomId, Long roomTypeId, String createdBy) {
        if (roomId == null) {
            return Reservation.rehydrate(
                    reservationId,
                    hotelId,
                    roomTypeId,
                    LocalDate.parse("2026-05-10"),
                    LocalDate.parse("2026-05-12"),
                    party(2),
                    ReservationStatus.PENDING,
                    Instant.parse("2026-04-03T12:00:00Z"),
                    null,
                    createdBy
            );
        }
        return Reservation.rehydrate(
                reservationId,
                hotelId,
                roomId,
                roomTypeId,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                ReservationStatus.PENDING,
                Instant.parse("2026-04-03T12:00:00Z"),
                null,
                createdBy
        );
    }
}
