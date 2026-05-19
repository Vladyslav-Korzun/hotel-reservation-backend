package com.hotel.management.mapper;

import com.hotel.management.api.dto.AvailableRoomResponse;
import com.hotel.management.api.dto.BookingPet;
import com.hotel.management.api.dto.RoomAmenityCode;
import com.hotel.management.api.dto.RoomTypeAvailabilityCalendarDayResponse;
import com.hotel.management.api.dto.SearchAvailableRoomsRequest;
import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.domain.shared.value.PetSize;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.domain.shared.value.StayPeriod;
import com.hotel.management.service.availability.AvailableRoomResult;
import com.hotel.management.service.availability.GetRoomTypeAvailabilityCalendarQuery;
import com.hotel.management.service.availability.RoomTypeAvailabilityCalendarDayResult;
import com.hotel.management.service.availability.SearchAvailableRoomsCommand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class AvailabilityMapper {

    public SearchAvailableRoomsCommand toCommand(SearchAvailableRoomsRequest request) {
        return new SearchAvailableRoomsCommand(
                request.getCity(),
                request.getHotelId(),
                new StayPeriod(request.getCheckIn(), request.getCheckOut()),
                new AccommodationParty(
                        new GuestComposition(request.getAdults(), request.getChildrenAges()),
                        toPetDetails(request.getPets())
                )
        );
    }

    public GetRoomTypeAvailabilityCalendarQuery toCalendarQuery(
            Long hotelId,
            Long roomTypeId,
            LocalDate from,
            LocalDate to
    ) {
        return new GetRoomTypeAvailabilityCalendarQuery(hotelId, roomTypeId, from, to);
    }

    public List<AvailableRoomResponse> toResponse(List<AvailableRoomResult> result) {
        return result.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RoomTypeAvailabilityCalendarDayResponse> toCalendarResponse(List<RoomTypeAvailabilityCalendarDayResult> result) {
        return result.stream()
                .map(this::toCalendarResponse)
                .toList();
    }

    private AvailableRoomResponse toResponse(AvailableRoomResult result) {
        return new AvailableRoomResponse()
                .hotelId(result.hotelId())
                .hotelName(result.hotelName())
                .roomTypeId(result.roomTypeId())
                .roomTypeName(result.roomTypeName())
                .maxAdults(result.maxAdults())
                .maxChildren(result.maxChildren())
                .maxInfants(result.maxInfants())
                .maxTotalGuests(result.maxTotalGuests())
                .petsAllowed(result.petsAllowed())
                .maxPets(result.maxPets())
                .basePriceAmount(result.basePrice().amount())
                .basePriceCurrency(result.basePrice().currency().getCurrencyCode())
                .availableCount(result.availableCount())
                .bedSetup(result.bedSetup())
                .roomSizeSqm(toFloat(result.roomSizeSqm()))
                .amenities(toAmenityCodes(result.amenities()));
    }

    private RoomTypeAvailabilityCalendarDayResponse toCalendarResponse(RoomTypeAvailabilityCalendarDayResult result) {
        return new RoomTypeAvailabilityCalendarDayResponse()
                .date(result.date())
                .availableCount(result.availableCount())
                .available(result.available());
    }

    private List<PetDetails> toPetDetails(List<BookingPet> pets) {
        if (pets == null) {
            return List.of();
        }
        return pets.stream()
                .map(pet -> new PetDetails(
                        PetType.valueOf(pet.getType().getValue()),
                        PetSize.valueOf(pet.getSize().getValue()),
                        pet.getWeightKg()
                ))
                .toList();
    }

    private List<RoomAmenityCode> toAmenityCodes(Set<RoomAmenity> value) {
        if (value == null) {
            return List.of();
        }
        return value.stream()
                .sorted()
                .map(amenity -> RoomAmenityCode.fromValue(amenity.name()))
                .toList();
    }

    private Float toFloat(BigDecimal value) {
        return value == null ? null : value.floatValue();
    }
}
