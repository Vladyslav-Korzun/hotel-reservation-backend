package com.hotel.management.jpa.serviceoffering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface JpaServiceOfferingSpringDataRepository extends JpaRepository<JpaServiceOfferingEntity, Long> {

    List<JpaServiceOfferingEntity> findByHotelId(Long hotelId);

    List<JpaServiceOfferingEntity> findByHotelIdAndActiveTrue(Long hotelId);
}
