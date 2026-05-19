package com.hotel.management.service.availability;

import java.util.List;
import com.hotel.management.domain.room.AvailableRoomResult;

public interface SearchAvailabilityFacade {

    List<AvailableRoomResult> searchAvailableRooms(SearchAvailableRoomsCommand command);
}