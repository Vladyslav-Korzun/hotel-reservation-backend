package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationFactory;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.EmailAddress;
import com.hotel.management.domain.shared.value.StayPeriod;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.predicate.reservation.IsAdminPredicate;
import com.hotel.management.domain.predicate.reservation.IsGuestPredicate;
import com.hotel.management.domain.predicate.reservation.IsReservationOwnerPredicate;
import com.hotel.management.domain.predicate.reservation.IsStaffOrAdminPredicate;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.shared.security.AuthenticatedUser;
import com.hotel.management.domain.shared.security.CurrentUserPort;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.domain.service.mapper.ReservationResultMapper;

public class ReservationService implements ReservationFacade {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final ReservationLockPort reservationLockPort;
    private final ClockPort clockPort;
    private final CurrentUserPort currentUserPort;
    private final ReservationCreationValidator reservationCreationValidator;
    private final ReservationFactory reservationFactory;
    private final ReservationPricingCalculator reservationPricingCalculator;
    private final ReservationResultMapper reservationResultMapper;
    private final AuditTrail auditTrail;

    public ReservationService(
            ReservationRepository reservationRepository,
            GuestRepository guestRepository,
            ReservationLockPort reservationLockPort,
            ClockPort clockPort,
            CurrentUserPort currentUserPort,
            ReservationCreationValidator reservationCreationValidator,
            ReservationFactory reservationFactory,
            ReservationPricingCalculator reservationPricingCalculator,
            ReservationResultMapper reservationResultMapper,
            AuditTrail auditTrail
    ) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.reservationLockPort = reservationLockPort;
        this.clockPort = clockPort;
        this.currentUserPort = currentUserPort;
        this.reservationCreationValidator = reservationCreationValidator;
        this.reservationFactory = reservationFactory;
        this.reservationPricingCalculator = reservationPricingCalculator;
        this.reservationResultMapper = reservationResultMapper;
        this.auditTrail = auditTrail;
    }

    @Override
    public CreateReservationResult createReservation(CreateReservationCommand command) {
        requireCommand(command);
        var currentUser = currentUserPort.getCurrentUser();
        Long guestId = resolveSelfBookingGuestId(currentUser);
        return createResolvedReservation(ResolvedCreateReservationCommand.from(command, guestId), currentUser);
    }

    @Override
    public CreateReservationResult createPublicReservation(CreatePublicReservationCommand command) {
        requireCommand(command);
        Guest guest = findOrCreateGuest(command.guestContact());
        var actor = new AuthenticatedUser("public:" + guest.id(), Set.of("PUBLIC"), guest.id());
        return createResolvedReservation(
                ResolvedCreateReservationCommand.from(command.toReservationCommand(), guest.id()),
                actor
        );
    }

    @Override
    public CreateReservationResult createStaffReservation(CreateStaffReservationCommand command) {
        requireCommand(command);
        var currentUser = currentUserPort.getCurrentUser();
        assertCanCreateStaffReservation(currentUser);
        Long guestId = resolveStaffBookingGuestId(command);
        return createResolvedReservation(
                ResolvedCreateReservationCommand.from(command.toReservationCommand(), guestId),
                currentUser
        );
    }

    private CreateReservationResult createResolvedReservation(
            ResolvedCreateReservationCommand command,
            AuthenticatedUser actor
    ) {
        ReservationCreationDetails creationDetails = reservationCreationValidator.validate(command);
        String reservationId = UUID.randomUUID().toString();
        var serviceItems = reservationFactory.createServiceItems(
                reservationId,
                creationDetails.serviceOfferings(),
                creationDetails.selections()
        );
        var stayPeriod = new StayPeriod(command.checkIn(), command.checkOut());
        var priceSnapshot = reservationPricingCalculator.calculate(
                creationDetails.roomType(),
                stayPeriod,
                serviceItems
        );
        var reservation = reservationFactory.createPendingReservation(
                reservationId,
                command.hotelId(),
                command.guestId(),
                command.roomTypeId(),
                command.checkIn(),
                command.checkOut(),
                command.accommodationParty(),
                resolveContactEmail(command, creationDetails),
                resolveContactPhone(command, creationDetails),
                command.specialRequests(),
                priceSnapshot,
                serviceItems,
                clockPort.now(),
                actor.userId()
        );

        Reservation savedReservation = reservationRepository.save(reservation);
        auditTrail.record(
                actor,
                AuditActionType.CREATE_RESERVATION,
                AuditEntityType.RESERVATION,
                savedReservation.id(),
                "Reservation created"
        );
        return reservationResultMapper.toCreateResult(savedReservation);
    }

    @Override
    public List<GetReservationResult> listReservations(int limit) {
        var currentUser = currentUserPort.getCurrentUser();
        assertCanList(currentUser);
        validateLimit(limit);

        return reservationRepository.findAll(limit).stream()
                .map(reservationResultMapper::toGetResult)
                .toList();
    }

    @Override
    public List<GetReservationResult> listMyReservations(int limit) {
        var currentUser = currentUserPort.getCurrentUser();
        validateLimit(limit);

        return reservationRepository.findByCreatedBy(currentUser.userId(), limit).stream()
                .map(reservationResultMapper::toGetResult)
                .toList();
    }

    @Override
    public GetReservationResult getReservation(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        var reservation = loadReservation(reservationId);
        assertCanView(reservation, currentUser);
        return reservationResultMapper.toGetResult(reservation);
    }

    @Override
    public void cancelReservation(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        var reservation = loadReservationForChange(reservationId);
        assertCanManage(reservation, currentUser);
        Reservation cancelledReservation = reservationRepository.save(reservation.cancel(clockPort.now()));
        auditTrail.record(
                currentUser,
                AuditActionType.CANCEL_RESERVATION,
                AuditEntityType.RESERVATION,
                cancelledReservation.id(),
                "Reservation cancelled"
        );
    }

    private Reservation loadReservation(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ValidationException("reservationId is required");
        }

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
    }

    private Reservation loadReservationForChange(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ValidationException("reservationId is required");
        }

        return reservationLockPort.findReservationForChange(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
    }

    private void assertCanView(Reservation reservation, AuthenticatedUser currentUser) {
        if (IsStaffOrAdminPredicate.INSTANCE.test(currentUser)) {
            return;
        }
        if (!IsReservationOwnerPredicate.INSTANCE.test(reservation, currentUser)) {
            throw new ForbiddenException("Access to this reservation is denied");
        }
    }

    private void assertCanList(AuthenticatedUser currentUser) {
        if (IsStaffOrAdminPredicate.INSTANCE.test(currentUser)) {
            return;
        }

        throw new ForbiddenException("Access to all reservations is denied");
    }

    private Long resolveSelfBookingGuestId(AuthenticatedUser currentUser) {
        if (!IsGuestPredicate.INSTANCE.test(currentUser)) {
            throw new ForbiddenException("Only guest can create reservation");
        }

        if (currentUser.guestId() == null) {
            throw new ForbiddenException("guestId claim is required to create reservation");
        }

        return currentUser.guestId();
    }

    private void assertCanCreateStaffReservation(AuthenticatedUser currentUser) {
        if (IsStaffOrAdminPredicate.INSTANCE.test(currentUser)) {
            return;
        }
        throw new ForbiddenException("Only staff or admin can create staff reservation");
    }

    private Long resolveStaffBookingGuestId(CreateStaffReservationCommand command) {
        if (command.guestId() != null) {
            return command.guestId();
        }
        return findOrCreateGuest(command.guestContact()).id();
    }

    private Guest findOrCreateGuest(GuestContactCommand guestContact) {
        if (guestContact == null) {
            throw new ValidationException("guest contact is required");
        }

        return guestRepository.findByEmail(guestContact.emailAddress())
                .orElseGet(() -> guestRepository.save(new Guest(
                        null,
                        guestContact.firstName(),
                        guestContact.lastName(),
                        guestContact.emailAddress(),
                        guestContact.phone()
                )));
    }

    private EmailAddress resolveContactEmail(
            ResolvedCreateReservationCommand command,
            ReservationCreationDetails creationDetails
    ) {
        String value = trimToNull(command.contactEmail());
        if (value == null) {
            return creationDetails.guest().email();
        }
        return new EmailAddress(value);
    }

    private String resolveContactPhone(
            ResolvedCreateReservationCommand command,
            ReservationCreationDetails creationDetails
    ) {
        String value = trimToNull(command.contactPhone());
        return value == null ? creationDetails.guest().phone() : value;
    }

    private void requireCommand(Object command) {
        if (command == null) {
            throw new ValidationException("reservation command is required");
        }
    }

    private void validateLimit(int limit) {
        if (limit <= 0 || limit > 200) {
            throw new ValidationException("limit must be between 1 and 200");
        }
    }

    private void assertCanManage(Reservation reservation, AuthenticatedUser currentUser) {
        if (!IsAdminPredicate.INSTANCE.test(currentUser) && !IsReservationOwnerPredicate.INSTANCE.test(reservation, currentUser)) {
            throw new ForbiddenException("Access to this reservation is denied");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

}
