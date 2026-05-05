package com.hotel.management.service.reservation;

public interface RoomInventoryPort {

    long countBookableRoomsForReservation(Long hotelId, Long roomTypeId);
}
