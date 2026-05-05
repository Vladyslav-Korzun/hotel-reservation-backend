package com.hotel.management.jpa.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaRoomSpringDataRepository extends JpaRepository<JpaRoomEntity, Long> {

    List<JpaRoomEntity> findByHotelIdIn(List<Long> hotelIds);

    @Query(value = """
            select *
            from rooms
            where hotel_id = :hotelId
              and room_type_id = :roomTypeId
              and status = 'AVAILABLE'
            order by id
            for update
            """, nativeQuery = true)
    List<JpaRoomEntity> findBookableForReservationForUpdate(
            @Param("hotelId") Long hotelId,
            @Param("roomTypeId") Long roomTypeId
    );

    @Query(value = """
            select *
            from rooms
            where hotel_id = :hotelId
              and room_type_id = :roomTypeId
              and status = 'AVAILABLE'
            order by id
            for update skip locked
            limit 1
            """, nativeQuery = true)
    List<JpaRoomEntity> findAvailableForCheckIn(
            @Param("hotelId") Long hotelId,
            @Param("roomTypeId") Long roomTypeId
    );
}
