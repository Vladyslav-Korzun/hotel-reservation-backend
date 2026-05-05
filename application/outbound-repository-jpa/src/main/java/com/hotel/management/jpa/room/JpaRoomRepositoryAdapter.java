package com.hotel.management.jpa.room;

import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaRoomRepositoryAdapter implements RoomRepository {

    private final JpaRoomSpringDataRepository roomSpringDataRepository;

    public JpaRoomRepositoryAdapter(JpaRoomSpringDataRepository roomSpringDataRepository) {
        this.roomSpringDataRepository = roomSpringDataRepository;
    }

    @Override
    public Room save(Room room) {
        var entity = new JpaRoomEntity();
        entity.setId(room.id());
        entity.setHotelId(room.hotelId());
        entity.setNumber(room.number());
        entity.setRoomTypeId(room.roomTypeId());
        entity.setCapacity(room.capacity());
        entity.setStatus(room.status().name());
        return toDomain(roomSpringDataRepository.save(entity));
    }

    @Override
    public Optional<Room> findById(Long roomId) {
        return roomSpringDataRepository.findById(roomId)
                .map(this::toDomain);
    }

    @Override
    public List<Room> findByHotelIds(List<Long> hotelIds) {
        if (hotelIds == null || hotelIds.isEmpty()) {
            return List.of();
        }

        return roomSpringDataRepository.findByHotelIdIn(hotelIds).stream()
                .map(this::toDomain)
                .toList();
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
