package com.hotel.management.domain.reservation;

public interface RoomInventoryPort {

    long countBookableRoomsForReservation(Long hotelId, Long roomTypeId);
}
