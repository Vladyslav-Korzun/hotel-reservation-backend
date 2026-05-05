package com.hotel.management.service.reservation;

import com.hotel.management.service.exception.ForbiddenException;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.security.AuthenticatedUser;
import com.hotel.management.service.security.CurrentUserPort;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.StayPeriod;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public class ReservationService implements ReservationFacade {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryPort reservationQueryPort;
    private final ReservationLockPort reservationLockPort;
    private final HotelRepository hotelRepository;
    private final RoomInventoryPort roomInventoryPort;
    private final RoomTypeRepository roomTypeRepository;
    private final ClockPort clockPort;
    private final CurrentUserPort currentUserPort;
    private final AccommodationPolicyValidator accommodationPolicyValidator;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationQueryPort reservationQueryPort,
            ReservationLockPort reservationLockPort,
            HotelRepository hotelRepository,
            RoomInventoryPort roomInventoryPort,
            RoomTypeRepository roomTypeRepository,
            ClockPort clockPort,
            CurrentUserPort currentUserPort,
            AccommodationPolicyValidator accommodationPolicyValidator
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationQueryPort = reservationQueryPort;
        this.reservationLockPort = reservationLockPort;
        this.hotelRepository = hotelRepository;
        this.roomInventoryPort = roomInventoryPort;
        this.roomTypeRepository = roomTypeRepository;
        this.clockPort = clockPort;
        this.currentUserPort = currentUserPort;
        this.accommodationPolicyValidator = accommodationPolicyValidator;
    }

    @Override
    public CreateReservationResult createReservation(CreateReservationCommand command) {
        requireCommand(command);
        var stayPeriod = new StayPeriod(command.checkIn(), command.checkOut());
        assertStayPeriodIsNotInPast(stayPeriod);
        assertRoomTypeAvailable(command, stayPeriod);

        var currentUser = currentUserPort.getCurrentUser();
        var reservation = Reservation.createPending(
                UUID.randomUUID().toString(),
                command.hotelId(),
                command.roomTypeId(),
                command.checkIn(),
                command.checkOut(),
                command.accommodationParty(),
                clockPort.now(),
                currentUser.userId()
        );

        var savedReservation = reservationRepository.save(reservation);
        return new CreateReservationResult(
                savedReservation.id(),
                savedReservation.hotelId(),
                savedReservation.roomId(),
                savedReservation.roomTypeId(),
                savedReservation.checkIn(),
                savedReservation.checkOut(),
                savedReservation.accommodationParty().guests().adults(),
                savedReservation.accommodationParty().guests().childrenAges(),
                savedReservation.accommodationParty().pets(),
                savedReservation.status().name(),
                savedReservation.createdAt(),
                savedReservation.cancelledAt(),
                savedReservation.createdBy()
        );
    }

    private void assertRoomTypeAvailable(CreateReservationCommand command, StayPeriod stayPeriod) {
        var hotel = hotelRepository.findById(command.hotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + command.hotelId()));
        if (!hotel.isActive()) {
            throw new ValidationException("Hotel is not active");
        }

        RoomType roomType = roomTypeRepository.findByHotelIds(List.of(command.hotelId())).stream()
                .filter(candidate -> candidate.id().equals(command.roomTypeId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Room type not found: " + command.roomTypeId()));

        accommodationPolicyValidator.validate(hotel, roomType, command.accommodationParty());

        long bookableRooms = roomInventoryPort.countBookableRoomsForReservation(
                        command.hotelId(),
                        command.roomTypeId()
                );
        if (bookableRooms == 0) {
            throw new ValidationException("No rooms are available for this room type");
        }

        long activeReservations = reservationQueryPort.findActiveOverlapping(List.of(command.hotelId()), stayPeriod).stream()
                .filter(reservation -> reservation.roomTypeId().equals(command.roomTypeId()))
                .count();
        if (activeReservations >= bookableRooms) {
            throw new ValidationException("No rooms are available for the selected period");
        }
    }

    @Override
    public List<GetReservationResult> listReservations(int limit) {
        var currentUser = currentUserPort.getCurrentUser();
        assertCanList(currentUser);
        if (limit <= 0 || limit > 200) {
            throw new ValidationException("limit must be between 1 and 200");
        }

        return reservationRepository.findAll(limit).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public GetReservationResult getReservation(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        var reservation = loadReservation(reservationId);
        assertCanView(reservation, currentUser);
        return toResult(reservation);
    }

    @Override
    public void cancelReservation(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        var reservation = loadReservationForChange(reservationId);
        assertCanManage(reservation, currentUser);
        reservationRepository.save(reservation.cancel(clockPort.now()));
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
        if (currentUser.isAdmin() || currentUser.isStaff()) {
            return;
        }
        if (!reservation.belongsTo(currentUser.userId())) {
            throw new ForbiddenException("Access to this reservation is denied");
        }
    }

    private void assertCanList(AuthenticatedUser currentUser) {
        if (currentUser.isAdmin() || currentUser.isStaff()) {
            return;
        }

        throw new ForbiddenException("Access to all reservations is denied");
    }

    private void assertCanManage(Reservation reservation, AuthenticatedUser currentUser) {
        if (!currentUser.isAdmin() && !reservation.belongsTo(currentUser.userId())) {
            throw new ForbiddenException("Access to this reservation is denied");
        }
    }

    private void requireCommand(CreateReservationCommand command) {
        if (command == null) {
            throw new ValidationException("reservation command is required");
        }
    }

    private void assertStayPeriodIsNotInPast(StayPeriod stayPeriod) {
        var today = clockPort.now().atZone(ZoneOffset.UTC).toLocalDate();
        if (stayPeriod.checkIn().isBefore(today)) {
            throw new ValidationException("checkIn must not be in the past");
        }
    }

    private GetReservationResult toResult(Reservation reservation) {
        return new GetReservationResult(
                reservation.id(),
                reservation.hotelId(),
                reservation.roomId(),
                reservation.roomTypeId(),
                reservation.checkIn(),
                reservation.checkOut(),
                reservation.accommodationParty().guests().adults(),
                reservation.accommodationParty().guests().childrenAges(),
                reservation.accommodationParty().pets(),
                reservation.status().name(),
                reservation.createdAt(),
                reservation.cancelledAt(),
                reservation.createdBy()
        );
    }
}
