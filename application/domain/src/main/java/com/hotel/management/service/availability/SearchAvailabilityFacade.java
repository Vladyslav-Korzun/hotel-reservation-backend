package com.hotel.management.service.availability;

import java.util.List;

public interface SearchAvailabilityFacade {

    List<AvailableRoomResult> searchAvailableRooms(SearchAvailableRoomsCommand command);
}
