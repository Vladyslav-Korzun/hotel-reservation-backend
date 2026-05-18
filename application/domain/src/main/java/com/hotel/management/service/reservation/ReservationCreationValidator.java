package com.hotel.management.service.reservation;

import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.predicate.hotel.IsActiveHotelPredicate;
import com.hotel.management.domain.predicate.serviceoffering.BelongsToHotelPredicate;
import com.hotel.management.domain.predicate.serviceoffering.IsActiveServiceOfferingPredicate;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.StayPeriod;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.service.port.ClockPort;

import java.time.ZoneOffset;
import java.util.List;

public class ReservationCreationValidator {

    private final ReservationQueryPort reservationQueryPort;
    private final HotelRepository hotelRepository;
    private final GuestRepository guestRepository;
    private final RoomInventoryPort roomInventoryPort;
    private final RoomTypeRepository roomTypeRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ClockPort clockPort;
    private final AccommodationPolicyValidator accommodationPolicyValidator;

    public ReservationCreationValidator(
            ReservationQueryPort reservationQueryPort,
            HotelRepository hotelRepository,
            GuestRepository guestRepository,
            RoomInventoryPort roomInventoryPort,
            RoomTypeRepository roomTypeRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            ClockPort clockPort,
            AccommodationPolicyValidator accommodationPolicyValidator
    ) {
        this.reservationQueryPort = reservationQueryPort;
        this.hotelRepository = hotelRepository;
        this.guestRepository = guestRepository;
        this.roomInventoryPort = roomInventoryPort;
        this.roomTypeRepository = roomTypeRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.clockPort = clockPort;
        this.accommodationPolicyValidator = accommodationPolicyValidator;
    }

    public ReservationCreationDetails validate(ResolvedCreateReservationCommand command) {
        requireCommand(command);
        StayPeriod stayPeriod = new StayPeriod(command.checkIn(), command.checkOut());
        assertStayPeriodIsNotInPast(stayPeriod);
        Guest guest = assertGuestExists(command.guestId());
        RoomType roomType = assertRoomTypeAvailable(command, stayPeriod);
        List<ServiceOffering> serviceOfferings = loadAndValidateServiceOfferings(command);
        return new ReservationCreationDetails(guest, roomType, serviceOfferings, command.serviceOfferings());
    }

    private Guest assertGuestExists(Long guestId) {
        return guestRepository.findById(guestId)
                .orElseThrow(() -> new NotFoundException("Guest not found: " + guestId));
    }

    private RoomType assertRoomTypeAvailable(ResolvedCreateReservationCommand command, StayPeriod stayPeriod) {
        var hotel = hotelRepository.findById(command.hotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + command.hotelId()));
        if (!IsActiveHotelPredicate.INSTANCE.test(hotel)) {
            throw new ValidationException("Hotel is not active");
        }

        RoomType roomType = loadRoomType(command);
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
        return roomType;
    }

    private RoomType loadRoomType(ResolvedCreateReservationCommand command) {
        return roomTypeRepository.findByHotelIds(List.of(command.hotelId())).stream()
                .filter(candidate -> candidate.id().equals(command.roomTypeId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Room type not found: " + command.roomTypeId()));
    }

    private List<ServiceOffering> loadAndValidateServiceOfferings(ResolvedCreateReservationCommand command) {
        if (command.serviceOfferings().isEmpty()) {
            return List.of();
        }

        return command.serviceOfferings().stream()
                .map(ServiceOfferingSelection::serviceOfferingId)
                .distinct()
                .map(serviceOfferingId -> loadServiceOffering(serviceOfferingId, command.hotelId()))
                .toList();
    }

    private ServiceOffering loadServiceOffering(Long serviceOfferingId, Long hotelId) {
        ServiceOffering serviceOffering = serviceOfferingRepository.findById(serviceOfferingId)
                .orElseThrow(() -> new NotFoundException("Service offering not found: " + serviceOfferingId));
        if (!BelongsToHotelPredicate.INSTANCE.test(serviceOffering, hotelId)) {
            throw new ValidationException("Service offering does not belong to selected hotel");
        }
        if (!IsActiveServiceOfferingPredicate.INSTANCE.test(serviceOffering)) {
            throw new ValidationException("Inactive service offering cannot be selected");
        }
        return serviceOffering;
    }

    private void requireCommand(ResolvedCreateReservationCommand command) {
        if (command == null) {
            throw new ValidationException("reservation command is required");
        }
        if (command.guestId() == null) {
            throw new ValidationException("guestId is required");
        }
    }

    private void assertStayPeriodIsNotInPast(StayPeriod stayPeriod) {
        var today = clockPort.now().atZone(ZoneOffset.UTC).toLocalDate();
        if (stayPeriod.checkIn().isBefore(today)) {
            throw new ValidationException("checkIn must not be in the past");
        }
    }
}
