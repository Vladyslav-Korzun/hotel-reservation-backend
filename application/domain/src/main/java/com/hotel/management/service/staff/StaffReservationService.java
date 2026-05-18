package com.hotel.management.service.staff;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.stay.Stay;
import com.hotel.management.domain.stay.StayRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.service.exception.ForbiddenException;
import com.hotel.management.service.port.AuditLogPort;
import com.hotel.management.service.port.ClockPort;
import com.hotel.management.service.predicate.reservation.IsBeforeReservationCheckOutDatePredicate;
import com.hotel.management.service.predicate.reservation.IsCheckInDateReachedPredicate;
import com.hotel.management.service.predicate.reservation.IsStaffOrAdminPredicate;
import com.hotel.management.service.reservation.locking.ReservationLockPort;
import com.hotel.management.service.security.AuthenticatedUser;
import com.hotel.management.service.security.CurrentUserPort;

import java.time.ZoneOffset;

public class StaffReservationService implements StaffReservationFacade {

    private final ReservationRepository reservationRepository;
    private final ReservationLockPort reservationLockPort;
    private final RoomRepository roomRepository;
    private final StayRepository stayRepository;
    private final RoomAssignmentPort roomAssignmentPort;
    private final CurrentUserPort currentUserPort;
    private final ClockPort clockPort;
    private final AuditLogPort auditLogPort;
    private final StaffReservationResultMapper staffReservationResultMapper;

    public StaffReservationService(
            ReservationRepository reservationRepository,
            ReservationLockPort reservationLockPort,
            RoomRepository roomRepository,
            StayRepository stayRepository,
            RoomAssignmentPort roomAssignmentPort,
            CurrentUserPort currentUserPort,
            ClockPort clockPort,
            AuditLogPort auditLogPort,
            StaffReservationResultMapper staffReservationResultMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationLockPort = reservationLockPort;
        this.roomRepository = roomRepository;
        this.stayRepository = stayRepository;
        this.roomAssignmentPort = roomAssignmentPort;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
        this.auditLogPort = auditLogPort;
        this.staffReservationResultMapper = staffReservationResultMapper;
    }

    @Override
    public StaffReservationResult checkIn(String reservationId) {
        var currentUser = requireStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        assertCheckInDateAllowed(reservation);
        var room = findAvailableRoomFor(reservation);

        var checkedInReservation = reservation.checkIn(room.id());
        reservationRepository.save(checkedInReservation);
        roomRepository.save(room.occupy());
        stayRepository.save(Stay.start(null, checkedInReservation.id(), room.id(), clockPort.now()));
        auditLogPort.append(auditEntry(
                currentUser,
                AuditActionType.CHECK_IN,
                checkedInReservation.id(),
                "Reservation checked in"
        ));

        return staffReservationResultMapper.toResult(checkedInReservation);
    }

    @Override
    public StaffReservationResult checkOut(String reservationId) {
        var currentUser = requireStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        var checkedOutReservation = reservation.completeCheckOut();
        var stay = stayRepository.findActiveByReservationId(reservation.id())
                .orElseThrow(() -> new ValidationException("Active stay not found for reservation"));
        var room = roomRepository.findById(reservation.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found: " + reservation.roomId()));

        reservationRepository.save(checkedOutReservation);
        stayRepository.save(stay.complete(clockPort.now()));
        roomRepository.save(room.markCleaningAfterCheckOut());
        auditLogPort.append(auditEntry(
                currentUser,
                AuditActionType.CHECK_OUT,
                checkedOutReservation.id(),
                "Reservation checked out"
        ));

        return staffReservationResultMapper.toResult(checkedOutReservation);
    }

    @Override
    public StaffReservationResult markNoShow(String reservationId) {
        var currentUser = requireStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        assertNoShowDateAllowed(reservation);

        var noShowReservation = reservation.markNoShow();
        reservationRepository.save(noShowReservation);
        auditLogPort.append(auditEntry(
                currentUser,
                AuditActionType.MARK_NO_SHOW,
                noShowReservation.id(),
                "Reservation marked as no-show"
        ));

        return staffReservationResultMapper.toResult(noShowReservation);
    }

    private AuthenticatedUser requireStaffOrAdmin() {
        var currentUser = currentUserPort.getCurrentUser();
        if (IsStaffOrAdminPredicate.INSTANCE.test(currentUser)) {
            return currentUser;
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
        if (!IsCheckInDateReachedPredicate.INSTANCE.test(reservation, today)) {
            throw new ValidationException("Check-in is not allowed before reservation check-in date");
        }
        if (!IsBeforeReservationCheckOutDatePredicate.INSTANCE.test(reservation, today)) {
            throw new ValidationException("Check-in is not allowed after reservation check-out date");
        }
    }

    private void assertNoShowDateAllowed(Reservation reservation) {
        var today = clockPort.now().atZone(ZoneOffset.UTC).toLocalDate();
        if (!IsCheckInDateReachedPredicate.INSTANCE.test(reservation, today)) {
            throw new ValidationException("No-show is not allowed before reservation check-in date");
        }
    }

    private AuditLogEntry auditEntry(
            AuthenticatedUser currentUser,
            AuditActionType actionType,
            String reservationId,
            String details
    ) {
        return new AuditLogEntry(
                null,
                currentUser.userId(),
                currentUser.roles().stream().findFirst().orElse("UNKNOWN"),
                actionType,
                AuditEntityType.RESERVATION,
                reservationId,
                clockPort.now(),
                details
        );
    }

}
