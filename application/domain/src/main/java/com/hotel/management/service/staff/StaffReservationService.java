package com.hotel.management.service.staff;

import com.hotel.management.service.exception.ForbiddenException;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.security.CurrentUserPort;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;

import java.time.ZoneOffset;

public class StaffReservationService implements StaffReservationFacade {

    private final ReservationRepository reservationRepository;
    private final ReservationLockPort reservationLockPort;
    private final RoomRepository roomRepository;
    private final RoomAssignmentPort roomAssignmentPort;
    private final CurrentUserPort currentUserPort;
    private final ClockPort clockPort;

    public StaffReservationService(
            ReservationRepository reservationRepository,
            ReservationLockPort reservationLockPort,
            RoomRepository roomRepository,
            RoomAssignmentPort roomAssignmentPort,
            CurrentUserPort currentUserPort,
            ClockPort clockPort
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationLockPort = reservationLockPort;
        this.roomRepository = roomRepository;
        this.roomAssignmentPort = roomAssignmentPort;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
    }

    @Override
    public StaffReservationResult checkIn(String reservationId) {
        assertStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        assertCheckInDateAllowed(reservation);
        var room = findAvailableRoomFor(reservation);

        var checkedInReservation = reservation.checkIn(room.id());
        reservationRepository.save(checkedInReservation);
        roomRepository.save(room.occupy());

        return toResult(checkedInReservation);
    }

    @Override
    public StaffReservationResult checkOut(String reservationId) {
        assertStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        var checkedOutReservation = reservation.completeCheckOut();
        var room = roomRepository.findById(reservation.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found: " + reservation.roomId()));

        reservationRepository.save(checkedOutReservation);
        roomRepository.save(room.markCleaningAfterCheckOut());

        return toResult(checkedOutReservation);
    }

    private void assertStaffOrAdmin() {
        var currentUser = currentUserPort.getCurrentUser();
        if (currentUser.isStaff() || currentUser.isAdmin()) {
            return;
        }
        throw new ForbiddenException("Only staff or admin can manage stay operations");
    }

    private Reservation loadReservationForChange(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new ValidationException("reservationId is required");
        }
        return reservationLockPort.findReservationForChange(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
    }

    private Room findAvailableRoomFor(Reservation reservation) {
        return roomAssignmentPort.findAvailableRoomForCheckIn(
                        reservation.hotelId(),
                        reservation.roomTypeId()
                )
                .orElseThrow(() -> new ValidationException("No available room for check-in"));
    }

    private void assertCheckInDateAllowed(Reservation reservation) {
        var today = clockPort.now().atZone(ZoneOffset.UTC).toLocalDate();
        if (today.isBefore(reservation.checkIn())) {
            throw new ValidationException("Check-in is not allowed before reservation check-in date");
        }
        if (!today.isBefore(reservation.checkOut())) {
            throw new ValidationException("Check-in is not allowed after reservation check-out date");
        }
    }

    private StaffReservationResult toResult(Reservation reservation) {
        return new StaffReservationResult(
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
