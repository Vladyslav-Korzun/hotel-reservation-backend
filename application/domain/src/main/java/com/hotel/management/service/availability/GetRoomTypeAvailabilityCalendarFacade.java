package com.hotel.management.service.availability;

import java.util.List;
import com.hotel.management.domain.room.RoomTypeAvailabilityCalendarDayResult;

public interface GetRoomTypeAvailabilityCalendarFacade {

    List<RoomTypeAvailabilityCalendarDayResult> getAvailabilityCalendar(GetRoomTypeAvailabilityCalendarQuery query);
}