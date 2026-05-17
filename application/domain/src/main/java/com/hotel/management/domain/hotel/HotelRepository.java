package com.hotel.management.domain.hotel;

import java.util.List;
import java.util.Optional;

public interface HotelRepository {

    Hotel save(Hotel hotel);

    Optional<Hotel> findById(Long hotelId);

    List<Hotel> findAllActive();

    List<Hotel> findActiveByCity(String city);
}
