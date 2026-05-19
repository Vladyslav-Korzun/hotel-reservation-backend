package com.hotel.management.domain.service.reservation;

import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.guest.Guest;

import java.util.List;

public record ReservationCreationDetails(
        Guest guest,
        RoomType roomType,
        List<ServiceOffering> serviceOfferings,
        List<ServiceOfferingSelection> selections
) {

    public ReservationCreationDetails {
        serviceOfferings = serviceOfferings == null ? List.of() : List.copyOf(serviceOfferings);
        selections = selections == null ? List.of() : List.copyOf(selections);
    }
}
