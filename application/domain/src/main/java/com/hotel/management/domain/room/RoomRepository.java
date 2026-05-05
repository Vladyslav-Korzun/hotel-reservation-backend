package com.hotel.management.domain.room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    Room save(Room room);

    Optional<Room> findById(Long roomId);

    List<Room> findByHotelIds(List<Long> hotelIds);
}
