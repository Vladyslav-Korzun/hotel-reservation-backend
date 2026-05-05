package com.hotel.management.jpa.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaHotelSpringDataRepository extends JpaRepository<JpaHotelEntity, Long> {

    List<JpaHotelEntity> findByStatusOrderByNameAsc(String status);

    List<JpaHotelEntity> findByCityIgnoreCaseAndStatusOrderByNameAsc(String city, String status);
}
