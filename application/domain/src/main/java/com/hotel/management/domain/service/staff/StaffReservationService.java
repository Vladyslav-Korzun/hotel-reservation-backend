package com.hotel.management.domain.service.staff;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditTrail;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.stay.Stay;
import com.hotel.management.domain.stay.StayRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.ClockPort;
import com.hotel.management.domain.predicate.reservation.IsBeforeReservationCheckOutDatePredicate;
import com.hotel.management.domain.predicate.reservation.IsCheckInDateReachedPredicate;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.shared.security.CurrentUserPort;

import java.time.ZoneOffset;
import com.hotel.management.domain.reservation.RoomAssignmentPort;
import com.hotel.management.domain.reservation.StaffReservationResult;
import com.hotel.management.domain.service.mapper.StaffReservationResultMapper;

public class StaffReservationService implements StaffReservationFacade {

    private final ReservationRepository reservationRepository;
    private final ReservationLockPort reservationLockPort;
    private final RoomRepository roomRepository;
    private final StayRepository stayRepository;
    private final RoomAssignmentPort roomAssignmentPort;
    private final CurrentUserPort currentUserPort;
    private final ClockPort clockPort;
    private final AuditTrail auditTrail;
    private final StaffReservationResultMapper staffReservationResultMapper;

    public StaffReservationService(
            ReservationRepository reservationRepository,
            ReservationLockPort reservationLockPort,
            RoomRepository roomRepository,
            StayRepository stayRepository,
            RoomAssignmentPort roomAssignmentPort,
            CurrentUserPort currentUserPort,
            ClockPort clockPort,
            AuditTrail auditTrail,
            StaffReservationResultMapper staffReservationResultMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationLockPort = reservationLockPort;
        this.roomRepository = roomRepository;
        this.stayRepository = stayRepository;
        this.roomAssignmentPort = roomAssignmentPort;
        this.currentUserPort = currentUserPort;
        this.clockPort = clockPort;
        this.auditTrail = auditTrail;
        this.staffReservationResultMapper = staffReservationResultMapper;
    }

    @Override
    public StaffReservationResult checkIn(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        currentUser.requireStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        assertCheckInDateAllowed(reservation);
        var room = findAvailableRoomFor(reservation);

        var checkedInReservation = reservation.checkIn(room.id());
        reservationRepository.save(checkedInReservation);
        roomRepository.save(room.occupy());
        stayRepository.save(Stay.start(null, checkedInReservation.id(), room.id(), clockPort.now()));
        auditTrail.record(
                currentUser,
                AuditActionType.CHECK_IN,
                AuditEntityType.RESERVATION,
                checkedInReservation.id(),
                "Reservation checked in"
        );

        return staffReservationResultMapper.toResult(checkedInReservation);
    }

    @Override
    public StaffReservationResult checkOut(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        currentUser.requireStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        var checkedOutReservation = reservation.completeCheckOut();
        var stay = stayRepository.findActiveByReservationId(reservation.id())
                .orElseThrow(() -> new ValidationException("Active stay not found for reservation"));
        var room = roomRepository.findById(reservation.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found: " + reservation.roomId()));

        reservationRepository.save(checkedOutReservation);
        stayRepository.save(stay.complete(clockPort.now()));
        roomRepository.save(room.markCleaningAfterCheckOut());
        auditTrail.record(
                currentUser,
                AuditActionType.CHECK_OUT,
                AuditEntityType.RESERVATION,
                checkedOutReservation.id(),
                "Reservation checked out"
        );

        return staffReservationResultMapper.toResult(checkedOutReservation);
    }

    @Override
    public StaffReservationResult markNoShow(String reservationId) {
        var currentUser = currentUserPort.getCurrentUser();
        currentUser.requireStaffOrAdmin();
        var reservation = loadReservationForChange(reservationId);
        assertNoShowDateAllowed(reservation);

        var noShowReservation = reservation.markNoShow();
        reservationRepository.save(noShowReservation);
        auditTrail.record(
                currentUser,
                AuditActionType.MARK_NO_SHOW,
                AuditEntityType.RESERVATION,
                noShowReservation.id(),
                "Reservation marked as no-show"
        );

        return staffReservationResultMapper.toResult(noShowReservation);
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

}
