package com.hotel.management.domain.room;

import com.hotel.management.domain.shared.value.Money;

public class RoomTypeFactory {

    public RoomType create(
            Long roomTypeId,
            Long hotelId,
            String name,
            OccupancyPolicy occupancyPolicy,
            PetPolicy petPolicy,
            Money basePrice,
            String description
    ) {
        return new RoomType(roomTypeId, hotelId, name, occupancyPolicy, petPolicy, basePrice, description);
    }
}
