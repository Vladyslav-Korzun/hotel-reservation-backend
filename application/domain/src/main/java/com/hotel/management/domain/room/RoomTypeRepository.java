package com.hotel.management.domain.room;

import java.util.List;

public interface RoomTypeRepository {

    List<RoomType> findByHotelIds(List<Long> hotelIds);
}
