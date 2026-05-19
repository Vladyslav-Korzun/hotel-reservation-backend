package com.hotel.management.controller;

import com.hotel.management.api.RoomsApi;
import com.hotel.management.api.dto.AvailableRoomResponse;
import com.hotel.management.api.dto.SearchAvailableRoomsRequest;
import com.hotel.management.service.availability.SearchAvailabilityFacade;
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
        var command = availabilityMapper.toCommand(searchAvailableRoomsRequest);
        var result = searchAvailabilityFacade.searchAvailableRooms(command);
        return ResponseEntity.ok(availabilityMapper.toResponse(result));
    }
}
