package com.hotel.management.jpa.room;

import com.hotel.management.service.staff.RoomAssignmentPort;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaRoomAssignmentAdapter implements RoomAssignmentPort {

    private final JpaRoomSpringDataRepository roomSpringDataRepository;

    public JpaRoomAssignmentAdapter(JpaRoomSpringDataRepository roomSpringDataRepository) {
        this.roomSpringDataRepository = roomSpringDataRepository;
    }

    @Override
    public Optional<Room> findAvailableRoomForCheckIn(Long hotelId, Long roomTypeId) {
        return roomSpringDataRepository.findAvailableForCheckIn(hotelId, roomTypeId).stream()
                .findFirst()
                .map(this::toDomain);
    }

    private Room toDomain(JpaRoomEntity entity) {
        return new Room(
                entity.getId(),
                entity.getHotelId(),
                entity.getNumber(),
                entity.getRoomTypeId(),
                entity.getCapacity(),
                RoomStatus.valueOf(entity.getStatus())
        );
    }
}
