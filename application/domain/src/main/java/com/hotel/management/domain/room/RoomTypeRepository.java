package com.hotel.management.domain.room;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository {

    RoomType save(RoomType roomType);

    Optional<RoomType> findById(Long roomTypeId);

    List<RoomType> findByHotelIds(List<Long> hotelIds);
}
