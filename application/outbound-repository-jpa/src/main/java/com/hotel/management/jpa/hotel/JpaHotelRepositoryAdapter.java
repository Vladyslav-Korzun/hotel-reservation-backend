package com.hotel.management.jpa.hotel;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaHotelRepositoryAdapter implements HotelRepository {

    private final JpaHotelSpringDataRepository hotelSpringDataRepository;

    public JpaHotelRepositoryAdapter(JpaHotelSpringDataRepository hotelSpringDataRepository) {
        this.hotelSpringDataRepository = hotelSpringDataRepository;
    }

    @Override
    public Optional<Hotel> findById(Long hotelId) {
        return hotelSpringDataRepository.findById(hotelId).map(this::toDomain);
    }

    @Override
    public List<Hotel> findAllActive() {
        return hotelSpringDataRepository.findByStatusOrderByNameAsc(HotelStatus.ACTIVE.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Hotel> findActiveByCity(String city) {
        return hotelSpringDataRepository.findByCityIgnoreCaseAndStatusOrderByNameAsc(city, HotelStatus.ACTIVE.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    private Hotel toDomain(JpaHotelEntity entity) {
        return new Hotel(
                entity.getId(),
                entity.getName(),
                entity.getCity(),
                entity.getCountry(),
                entity.getAddress(),
                entity.getStars(),
                entity.getDescription(),
                HotelStatus.valueOf(entity.getStatus()),
                new HotelPolicy(
                        Boolean.TRUE.equals(entity.getChildrenAllowed()),
                        Boolean.TRUE.equals(entity.getPetsAllowed()),
                        entity.getInfantMaxAge(),
                        entity.getChildMaxAge()
                )
        );
    }
}
