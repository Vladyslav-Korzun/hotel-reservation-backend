package com.hotel.management.jpa.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRoomTypeSpringDataRepository extends JpaRepository<JpaRoomTypeEntity, Long> {

    List<JpaRoomTypeEntity> findByHotelIdIn(List<Long> hotelIds);
}
