package com.hotel.management.jpa.room;

import com.hotel.management.service.reservation.RoomInventoryPort;
import org.springframework.stereotype.Component;

@Component
public class JpaRoomInventoryAdapter implements RoomInventoryPort {

    private final JpaRoomSpringDataRepository roomSpringDataRepository;

    public JpaRoomInventoryAdapter(JpaRoomSpringDataRepository roomSpringDataRepository) {
        this.roomSpringDataRepository = roomSpringDataRepository;
    }

    @Override
    public long countBookableRoomsForReservation(Long hotelId, Long roomTypeId) {
        return roomSpringDataRepository.findBookableForReservationForUpdate(hotelId, roomTypeId).size();
    }
}
