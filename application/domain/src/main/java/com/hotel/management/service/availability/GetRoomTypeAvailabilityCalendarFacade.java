package com.hotel.management.service.availability;

import java.util.List;

public interface GetRoomTypeAvailabilityCalendarFacade {

    List<RoomTypeAvailabilityCalendarDayResult> getAvailabilityCalendar(GetRoomTypeAvailabilityCalendarQuery query);
}
