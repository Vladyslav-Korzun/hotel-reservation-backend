package com.hotel.management.service.availability;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.service.accommodation.AccommodationPolicyValidator;
import com.hotel.management.service.reservation.ReservationQueryPort;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchAvailabilityService implements SearchAvailabilityFacade {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ReservationQueryPort reservationQueryPort;
    private final AccommodationPolicyValidator accommodationPolicyValidator;

    public SearchAvailabilityService(
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            ReservationQueryPort reservationQueryPort,
            AccommodationPolicyValidator accommodationPolicyValidator
    ) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.reservationQueryPort = reservationQueryPort;
        this.accommodationPolicyValidator = accommodationPolicyValidator;
    }

    @Override
    public List<AvailableRoomResult> searchAvailableRooms(SearchAvailableRoomsCommand command) {
        Objects.requireNonNull(command, "command is required");

        List<Hotel> hotels = loadHotels(command);
        if (hotels.isEmpty()) {
            return List.of();
        }

        List<Long> hotelIds = hotels.stream().map(Hotel::id).toList();
        Map<Long, Hotel> hotelsById = hotels.stream().collect(Collectors.toMap(Hotel::id, Function.identity()));
        Map<Long, RoomType> roomTypesById = roomTypeRepository.findByHotelIds(hotelIds).stream()
                .filter(roomType -> supports(hotelsById.get(roomType.hotelId()), roomType, command))
                .collect(Collectors.toMap(RoomType::id, Function.identity()));
        if (roomTypesById.isEmpty()) {
            return List.of();
        }

        Map<RoomTypeInventoryKey, Long> bookableRooms = roomRepository.findByHotelIds(hotelIds).stream()
                .filter(room -> room.isBookable())
                .filter(room -> roomTypesById.containsKey(room.roomTypeId()))
                .collect(Collectors.groupingBy(
                        room -> new RoomTypeInventoryKey(room.hotelId(), room.roomTypeId()),
                        Collectors.counting()
                ));

        Map<RoomTypeInventoryKey, Long> activeReservations = reservationQueryPort.findActiveOverlapping(hotelIds, command.stayPeriod()).stream()
                .collect(Collectors.groupingBy(
                        reservation -> new RoomTypeInventoryKey(reservation.hotelId(), reservation.roomTypeId()),
                        Collectors.counting()
                ));

        return bookableRooms.entrySet().stream()
                .map(entry -> toAvailableRoomResult(entry, activeReservations, hotelsById, roomTypesById))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(AvailableRoomResult::hotelId)
                        .thenComparing(AvailableRoomResult::roomTypeId))
                .toList();
    }

    private List<Hotel> loadHotels(SearchAvailableRoomsCommand command) {
        if (command.hotelId() != null) {
            Hotel hotel = hotelRepository.findById(command.hotelId())
                    .orElseThrow(() -> new NotFoundException("Hotel not found: " + command.hotelId()));
            return hotel.isActive() ? List.of(hotel) : List.of();
        }

        if (command.city() == null || command.city().isBlank()) {
            return hotelRepository.findAllActive();
        }

        return hotelRepository.findActiveByCity(command.city());
    }

    private AvailableRoomResult toAvailableRoomResult(
            Map.Entry<RoomTypeInventoryKey, Long> entry,
            Map<RoomTypeInventoryKey, Long> activeReservations,
            Map<Long, Hotel> hotelsById,
            Map<Long, RoomType> roomTypesById
    ) {
        RoomTypeInventoryKey key = entry.getKey();
        long availableCount = entry.getValue() - activeReservations.getOrDefault(key, 0L);
        if (availableCount <= 0) {
            return null;
        }

        Hotel hotel = hotelsById.get(key.hotelId());
        RoomType roomType = roomTypesById.get(key.roomTypeId());
        if (hotel == null || roomType == null) {
            return null;
        }

        return new AvailableRoomResult(
                hotel.id(),
                hotel.name(),
                roomType.id(),
                roomType.name(),
                roomType.occupancyPolicy().maxAdults(),
                roomType.occupancyPolicy().maxChildren(),
                roomType.occupancyPolicy().maxInfants(),
                roomType.occupancyPolicy().maxTotalGuests(),
                roomType.petPolicy().petsAllowed(),
                roomType.petPolicy().maxPets(),
                roomType.basePrice(),
                Math.toIntExact(availableCount)
        );
    }

    private boolean supports(Hotel hotel, RoomType roomType, SearchAvailableRoomsCommand command) {
        try {
            accommodationPolicyValidator.validate(hotel, roomType, command.accommodationParty());
            return true;
        } catch (ValidationException ex) {
            return false;
        }
    }

    private record RoomTypeInventoryKey(Long hotelId, Long roomTypeId) {
    }
}
