package com.hotel.management.controller;

import com.hotel.management.api.RoomsApi;
import com.hotel.management.api.dto.AvailableRoomResponse;
import com.hotel.management.api.dto.BookingPet;
import com.hotel.management.api.dto.SearchAvailableRoomsRequest;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.domain.shared.value.PetSize;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.service.availability.SearchAvailabilityFacade;
import com.hotel.management.service.availability.SearchAvailableRoomsCommand;
import com.hotel.management.domain.shared.value.StayPeriod;
import com.hotel.management.mapper.AvailabilityMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoomsController implements RoomsApi {

    private final SearchAvailabilityFacade searchAvailabilityFacade;
    private final AvailabilityMapper availabilityMapper;

    public RoomsController(
            SearchAvailabilityFacade searchAvailabilityFacade,
            AvailabilityMapper availabilityMapper
    ) {
        this.searchAvailabilityFacade = searchAvailabilityFacade;
        this.availabilityMapper = availabilityMapper;
    }

    @Override
    public ResponseEntity<List<AvailableRoomResponse>> searchAvailableRooms(SearchAvailableRoomsRequest searchAvailableRoomsRequest) {
        var command = new SearchAvailableRoomsCommand(
                searchAvailableRoomsRequest.getCity(),
                searchAvailableRoomsRequest.getHotelId(),
                new StayPeriod(searchAvailableRoomsRequest.getCheckIn(), searchAvailableRoomsRequest.getCheckOut()),
                new AccommodationParty(
                        new GuestComposition(
                                searchAvailableRoomsRequest.getAdults(),
                                searchAvailableRoomsRequest.getChildrenAges()
                        ),
                        mapPets(searchAvailableRoomsRequest.getPets())
                )
        );
        var result = searchAvailabilityFacade.searchAvailableRooms(command);
        return ResponseEntity.ok(availabilityMapper.toResponse(result));
    }

    private List<PetDetails> mapPets(List<BookingPet> pets) {
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
}
