package com.hotel.management.domain.service.availability;

import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.predicate.room.IsBookableRoomPredicate;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.reservation.BookedRoomTypePeriodView;
import com.hotel.management.domain.reservation.ReservationQueryPort;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;
import com.hotel.management.domain.room.RoomTypeAvailabilityCalendarDayResult;

public class GetRoomTypeAvailabilityCalendarService implements GetRoomTypeAvailabilityCalendarFacade {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final ReservationQueryPort reservationQueryPort;

    public GetRoomTypeAvailabilityCalendarService(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            RoomRepository roomRepository,
            ReservationQueryPort reservationQueryPort
    ) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public List<RoomTypeAvailabilityCalendarDayResult> getAvailabilityCalendar(GetRoomTypeAvailabilityCalendarQuery query) {
        Objects.requireNonNull(query, "query is required");
        hotelRepository.findById(query.hotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + query.hotelId()));
        RoomType roomType = roomTypeRepository.findById(query.roomTypeId())
                .orElseThrow(() -> new NotFoundException("Room type not found: " + query.roomTypeId()));
        if (!Objects.equals(roomType.hotelId(), query.hotelId())) {
            throw new ValidationException("Room type does not belong to hotel");
        }

        int totalRooms = countBookableRooms(query.hotelId(), query.roomTypeId());
        List<BookedRoomTypePeriodView> bookedPeriods = reservationQueryPort.findBookedRoomTypePeriods(
                query.hotelId(),
                query.roomTypeId(),
                query.from(),
                query.to().plusDays(1)
        );

        return datesBetweenInclusive(query.from(), query.to()).stream()
                .map(date -> toCalendarDay(date, totalRooms, bookedPeriods))
                .toList();
    }

    private RoomTypeAvailabilityCalendarDayResult toCalendarDay(
            LocalDate date,
            int totalRooms,
            List<BookedRoomTypePeriodView> bookedPeriods
    ) {
        int bookedCount = Math.toIntExact(bookedPeriods.stream()
                .filter(period -> occupiesDate(period, date))
                .count());
        int availableCount = Math.max(totalRooms - bookedCount, 0);
        return new RoomTypeAvailabilityCalendarDayResult(
                date,
                availableCount,
                availableCount > 0
        );
    }

    private int countBookableRooms(Long hotelId, Long roomTypeId) {
        return Math.toIntExact(roomRepository.findByHotelIds(List.of(hotelId)).stream()
                .filter(IsBookableRoomPredicate.INSTANCE)
                .filter(room -> Objects.equals(room.roomTypeId(), roomTypeId))
                .count());
    }

    private boolean occupiesDate(BookedRoomTypePeriodView period, LocalDate date) {
        return !date.isBefore(period.checkIn()) && date.isBefore(period.checkOut());
    }

    private List<LocalDate> datesBetweenInclusive(LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        return LongStream.range(0, days)
                .mapToObj(from::plusDays)
                .toList();
    }
}