package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationFactory;
import com.hotel.management.domain.reservation.ReservationPriceSnapshot;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.EmailAddress;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.domain.service.exception.ForbiddenException;
import com.hotel.management.domain.audit.AuditLogPort;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hotel.management.domain.reservation.ReservationQueryPort;
import com.hotel.management.domain.reservation.RoomInventoryPort;
import com.hotel.management.domain.reservation.ActiveReservationView;
import com.hotel.management.domain.service.mapper.ReservationResultMapper;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private ReservationQueryPort reservationQueryPort;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private ReservationLockPort reservationLockPort;

    @Mock
    private RoomInventoryPort roomInventoryPort;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private ServiceOfferingRepository serviceOfferingRepository;

    @Mock
    private ClockPort clockPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private AccommodationPolicyValidator accommodationPolicyValidator;

    @Mock
    private AuditLogPort auditLogPort;

    private ReservationCreationValidator reservationCreationValidator;

    private ReservationService facade;

    @BeforeEach
    void setUp() {
        reservationCreationValidator = new ReservationCreationValidator(
                reservationQueryPort,
                hotelRepository,
                guestRepository,
                roomInventoryPort,
                roomTypeRepository,
                serviceOfferingRepository,
                clockPort,
                accommodationPolicyValidator
        );
        facade = new ReservationService(
                repository,
                guestRepository,
                reservationLockPort,
                clockPort,
                currentUserPort,
                reservationCreationValidator,
                new ReservationFactory(),
                new ReservationPricingCalculator(),
                new ReservationResultMapper(),
                auditLogPort
        );
    }

    @Test
    void shouldCreateReservationForAuthenticatedUser() {
        var periodCheckIn = LocalDate.parse("2026-05-10");
        var periodCheckOut = LocalDate.parse("2026-05-12");
        stubAvailableRoomType(1L, 2L);
        when(reservationQueryPort.findActiveOverlapping(any(), any())).thenReturn(List.of());
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST"), 10L));
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
        assertEquals(10L, result.guestId());
        assertEquals("guest-123", result.createdBy());
        assertEquals(2, result.adults());
        assertEquals(Instant.parse("2026-04-03T12:00:00Z"), result.createdAt());
    }

    @Test
    void shouldCreateReservationWithServiceOfferingAndPriceSnapshot() {
        stubAvailableRoomType(1L, 2L);
        when(serviceOfferingRepository.findById(10001L)).thenReturn(Optional.of(new ServiceOffering(
                10001L,
                1L,
                "BREAKFAST",
                "Breakfast",
                "Buffet breakfast",
                Money.of("15.00", "EUR"),
                true,
                null
        )));
        when(reservationQueryPort.findActiveOverlapping(any(), any())).thenReturn(List.of());
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST"), 10L));
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                List.of(new ServiceOfferingSelection(10001L, 2))
        ));

        assertEquals(Money.of("200.00", "EUR"), result.basePrice());
        assertEquals(Money.of("30.00", "EUR"), result.servicesPrice());
        assertEquals(Money.of("230.00", "EUR"), result.finalPrice());
        assertEquals(1, result.serviceItems().size());
        assertEquals("Breakfast", result.serviceItems().getFirst().serviceName());
    }

    @Test
    void shouldCreatePublicReservationForExistingGuestByEmail() {
        stubAvailableRoomType(1L, 2L);
        when(guestRepository.findByEmail(new EmailAddress("john@example.com"))).thenReturn(Optional.of(new Guest(
                10L,
                "John",
                "Smith",
                new EmailAddress("john@example.com"),
                "+421900000000"
        )));
        when(reservationQueryPort.findActiveOverlapping(any(), any())).thenReturn(List.of());
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = facade.createPublicReservation(new CreatePublicReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                List.of(),
                new GuestContactCommand("John", "Smith", "john@example.com", "+421900000000")
        ));

        assertEquals(10L, result.guestId());
        assertEquals("public:10", result.createdBy());
        verify(currentUserPort, never()).getCurrentUser();
    }

    @Test
    void shouldCreateStaffReservationForSelectedGuest() {
        stubAvailableRoomType(1L, 2L);
        when(reservationQueryPort.findActiveOverlapping(any(), any())).thenReturn(List.of());
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-100", Set.of("STAFF")));
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = facade.createStaffReservation(new CreateStaffReservationCommand(
                1L,
                2L,
                10L,
                null,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                List.of()
        ));

        assertEquals(10L, result.guestId());
        assertEquals("staff-100", result.createdBy());
    }

    @Test
    void shouldRejectStaffReservationWithoutGuestIdOrGuestContact() {
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("staff-100", Set.of("STAFF")));

        assertThrows(ValidationException.class, () -> facade.createStaffReservation(new CreateStaffReservationCommand(
                1L,
                2L,
                null,
                null,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                List.of()
        )));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectGuestCreatingReservationWithoutGuestClaim() {
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ForbiddenException.class, () -> facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2)
        )));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectReservationForMissingGuest() {
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST"), 10L));
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-03T12:00:00Z"));
        when(guestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> facade.createReservation(new CreateReservationCommand(
                1L,
                2L,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2)
        )));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidStayPeriod() {
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST"), 10L));
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
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST"), 10L));

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
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST"), 10L));

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
        Reservation secondReservation = reservation(
                "reservation-2",
                3L,
                null,
                4L,
                ReservationStatus.CANCELLED,
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
    void shouldListCurrentUserReservations() {
        Reservation firstReservation = pendingReservation("reservation-1", 1L, null, 2L, "guest-123");
        Reservation secondReservation = pendingReservation("reservation-2", 1L, null, 2L, "guest-123");
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));
        when(repository.findByCreatedBy("guest-123", 100)).thenReturn(List.of(firstReservation, secondReservation));

        var result = facade.listMyReservations(100);

        assertEquals(2, result.size());
        assertEquals("guest-123", result.getFirst().createdBy());
        verify(repository, never()).findAll(100);
    }

    @Test
    void shouldRejectInvalidLimitForCurrentUserReservations() {
        when(currentUserPort.getCurrentUser()).thenReturn(new AuthenticatedUser("guest-123", Set.of("GUEST")));

        assertThrows(ValidationException.class, () -> facade.listMyReservations(0));
        verify(repository, never()).findByCreatedBy(any(), anyInt());
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
        Reservation reservation = reservation(
                "reservation-1",
                1L,
                null,
                2L,
                ReservationStatus.CANCELLED,
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
        Reservation reservation = reservation(
                "reservation-1",
                1L,
                100L,
                2L,
                ReservationStatus.CHECKED_IN,
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
        Reservation reservation = reservation(
                "reservation-1",
                1L,
                100L,
                2L,
                ReservationStatus.CHECKED_OUT,
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
        Reservation reservation = reservation(
                "reservation-1",
                1L,
                null,
                2L,
                ReservationStatus.NO_SHOW,
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
        assertThrows(ValidationException.class, () -> reservation(
                "reservation-1",
                1L,
                null,
                2L,
                ReservationStatus.CHECKED_IN,
                null,
                "guest-123"
        ));
    }

    @Test
    void shouldRejectCheckedOutReservationWithoutAssignedRoom() {
        assertThrows(ValidationException.class, () -> reservation(
                "reservation-1",
                1L,
                null,
                2L,
                ReservationStatus.CHECKED_OUT,
                null,
                "guest-123"
        ));
    }

    private void stubAvailableRoomType(Long hotelId, Long roomTypeId) {
        when(guestRepository.findById(10L)).thenReturn(Optional.of(new Guest(
                10L,
                "John",
                "Smith",
                new EmailAddress("john@example.com"),
                "+421900000000"
        )));
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(new Hotel(
                hotelId,
                "Danube Hotel",
                "Bratislava",
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                HotelStatus.ACTIVE,
                new HotelPolicy(true, true, 2, 12, 13)
        )));
        when(roomTypeRepository.findByHotelIds(List.of(hotelId))).thenReturn(List.of(new RoomType(
                roomTypeId,
                hotelId,
                "Standard",
                new OccupancyPolicy(2, 1, 1, 3),
                new PetPolicy(false, 0, Set.of(), null, null),
                Money.of("100.00", "EUR"),
                "Standard room",
                RoomTypeFeatures.empty()
        )));
        when(roomInventoryPort.countBookableRoomsForReservation(hotelId, roomTypeId)).thenReturn(1L);
    }

    private static AccommodationParty party(int adults, Integer... childrenAges) {
        return new AccommodationParty(new GuestComposition(adults, List.of(childrenAges)), List.of());
    }

    private static Reservation pendingReservation(String reservationId, Long hotelId, Long roomId, Long roomTypeId, String createdBy) {
        return reservation(reservationId, hotelId, roomId, roomTypeId, ReservationStatus.PENDING, null, createdBy);
    }

    private static Reservation reservation(
            String reservationId,
            Long hotelId,
            Long roomId,
            Long roomTypeId,
            ReservationStatus status,
            Instant cancelledAt,
            String createdBy
    ) {
        return Reservation.rehydrate(
                reservationId,
                hotelId,
                10L,
                roomId,
                roomTypeId,
                LocalDate.parse("2026-05-10"),
                LocalDate.parse("2026-05-12"),
                party(2),
                priceSnapshot(),
                List.of(),
                status,
                Instant.parse("2026-04-03T12:00:00Z"),
                cancelledAt,
                createdBy
        );
    }

    private static ReservationPriceSnapshot priceSnapshot() {
        Money zero = Money.zero("EUR");
        return new ReservationPriceSnapshot(zero, zero, zero, zero);
    }
}
