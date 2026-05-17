package com.hotel.management.domain.serviceoffering;

import java.util.List;
import java.util.Optional;

public interface ServiceOfferingRepository {

    Optional<ServiceOffering> findById(Long serviceOfferingId);

    List<ServiceOffering> findByHotelId(Long hotelId);

    List<ServiceOffering> findActiveByHotelId(Long hotelId);

    ServiceOffering save(ServiceOffering serviceOffering);
}
