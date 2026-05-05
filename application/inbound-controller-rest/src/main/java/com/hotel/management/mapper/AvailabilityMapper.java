package com.hotel.management.mapper;

import com.hotel.management.api.dto.AvailableRoomResponse;
import com.hotel.management.service.availability.AvailableRoomResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AvailabilityMapper {

    public List<AvailableRoomResponse> toResponse(List<AvailableRoomResult> result) {
        return result.stream()
                .map(this::toResponse)
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
                .availableCount(result.availableCount());
    }
}
