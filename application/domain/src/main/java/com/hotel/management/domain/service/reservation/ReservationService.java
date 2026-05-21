package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationAccessPolicy;
import com.hotel.management.domain.reservation.ReservationFactory;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.EmailAddress;
import com.hotel.management.domain.shared.value.StayPeriod;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.shared.security.AuthenticatedUser;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.domain.service.mapper.ReservationResultMapper;
import com.hotel.management.domain.service.staff.HotelScopePolicy;

public class ReservationService implements ReservationFacade {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final ReservationLockPort reservationLockPort;
    private final ClockPort clockPort;
    private final ReservationCreationValidator reservationCreationValidator;
    private final ReservationFactory reservationFactory;
    private final ReservationPricingCalculator reservationPricingCalculator;
    private final ReservationResultMapper reservationResultMapper;
    private final AuditTrail auditTrail;
    private final HotelScopePolicy hotelScopePolicy;

    public ReservationService(
            ReservationRepository reservationRepository,
            GuestRepository guestRepository,
            ReservationLockPort reservationLockPort,
            ClockPort clockPort,
            ReservationCreationValidator reservationCreationValidator,
            ReservationFactory reservationFactory,
            ReservationPricingCalculator reservationPricingCalculator,
            ReservationResultMapper reservationResultMapper,
            AuditTrail auditTrail,
            HotelScopePolicy hotelScopePolicy
    ) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.reservationLockPort = reservationLockPort;
        this.clockPort = clockPort;
        this.reservationCreationValidator = reservationCreationValidator;
        this.reservationFactory = reservationFactory;
        this.reservationPricingCalculator = reservationPricingCalculator;
        this.reservationResultMapper = reservationResultMapper;
        this.auditTrail = auditTrail;
        this.hotelScopePolicy = hotelScopePolicy;
    }

    @Override
    public CreateReservationResult createReservation(AuthenticatedUser actor, CreateReservationCommand command) {
        if (command == null) {
            throw new ValidationException("reservation command is required");
        }
        Long guestId = actor.requireGuestId();
        return createResolvedReservation(ResolvedCreateReservationCommand.from(command, guestId), actor);
    }

    @Override
    public CreateReservationResult createPublicReservation(CreatePublicReservationCommand command) {
        if (command == null) {
            throw new ValidationException("reservation command is required");
        }
        Guest guest = findOrCreateGuest(command.guestContact());
        var actor = new AuthenticatedUser("public:" + guest.id(), Set.of("PUBLIC"), guest.id());
        return createResolvedReservation(
                ResolvedCreateReservationCommand.from(command.toReservationCommand(), guest.id()),
                actor
        );
    }

    @Override
    public CreateReservationResult createStaffReservation(AuthenticatedUser actor, CreateStaffReservationCommand command) {
        if (command == null) {
            throw new ValidationException("reservation command is required");
        }
        actor.requireStaffOrAdmin();
        hotelScopePolicy.assertCanAccessHotel(actor, command.hotelId());
        Long guestId = resolveStaffBookingGuestId(command);
        return createResolvedReservation(
                ResolvedCreateReservationCommand.from(command.toReservationCommand(), guestId),
                actor
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
    public List<GetReservationResult> listReservations(AuthenticatedUser actor, int limit) {
        actor.requireStaffOrAdmin();
        validateLimit(limit);

        Long scopedHotelId = hotelScopePolicy.resolveAccessibleHotel(actor);
        List<Reservation> reservations = scopedHotelId == null
                ? reservationRepository.findAll(limit)
                : reservationRepository.findByHotelId(scopedHotelId, limit);
        return reservations.stream()
                .map(reservationResultMapper::toGetResult)
                .toList();
    }

    @Override
    public List<GetReservationResult> listMyReservations(AuthenticatedUser actor, int limit) {
        Long guestId = actor.requireGuestId();
        validateLimit(limit);

        return reservationRepository.findByGuestId(guestId, limit).stream()
                .map(reservationResultMapper::toGetResult)
                .toList();
    }

    @Override
    public GetReservationResult getReservation(AuthenticatedUser actor, String reservationId) {
        var reservation = loadReservation(reservationId);
        ReservationAccessPolicy.INSTANCE.assertCanView(actor, reservation);
        if (actor != null && actor.isStaff() && !actor.isAdmin()) {
            hotelScopePolicy.assertCanAccessHotel(actor, reservation.hotelId());
        }
        return reservationResultMapper.toGetResult(reservation);
    }

    @Override
    public void cancelReservation(AuthenticatedUser actor, String reservationId) {
        var reservation = loadReservationForChange(reservationId);
        ReservationAccessPolicy.INSTANCE.assertCanCancel(actor, reservation);
        Reservation cancelledReservation = reservationRepository.save(reservation.cancel(clockPort.now()));
        auditTrail.record(
                actor,
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

    private void validateLimit(int limit) {
        if (limit <= 0 || limit > 200) {
            throw new ValidationException("limit must be between 1 and 200");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

}
