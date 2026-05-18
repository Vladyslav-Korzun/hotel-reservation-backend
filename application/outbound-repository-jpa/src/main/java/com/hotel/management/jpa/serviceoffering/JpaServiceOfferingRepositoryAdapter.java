package com.hotel.management.jpa.serviceoffering;

import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.value.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Component
public class JpaServiceOfferingRepositoryAdapter implements ServiceOfferingRepository {

    private final JpaServiceOfferingSpringDataRepository repository;

    public JpaServiceOfferingRepositoryAdapter(JpaServiceOfferingSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ServiceOffering> findById(Long serviceOfferingId) {
        return repository.findById(serviceOfferingId).map(this::toDomain);
    }

    @Override
    public List<ServiceOffering> findByHotelId(Long hotelId) {
        return repository.findByHotelId(hotelId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ServiceOffering> findActiveByHotelId(Long hotelId) {
        return repository.findByHotelIdAndActiveTrue(hotelId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ServiceOffering save(ServiceOffering serviceOffering) {
        return toDomain(repository.save(toEntity(serviceOffering)));
    }

    private ServiceOffering toDomain(JpaServiceOfferingEntity entity) {
        return new ServiceOffering(
                entity.getId(),
                entity.getHotelId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                new Money(entity.getPriceAmount(), Currency.getInstance(entity.getPriceCurrency())),
                Boolean.TRUE.equals(entity.getActive()),
                entity.getAvailabilityRule()
        );
    }

    private JpaServiceOfferingEntity toEntity(ServiceOffering serviceOffering) {
        var entity = new JpaServiceOfferingEntity();
        entity.setId(serviceOffering.id());
        entity.setHotelId(serviceOffering.hotelId());
        entity.setCode(serviceOffering.code());
        entity.setName(serviceOffering.name());
        entity.setDescription(serviceOffering.description());
        entity.setPriceAmount(serviceOffering.price().amount());
        entity.setPriceCurrency(serviceOffering.price().currency().getCurrencyCode());
        entity.setActive(serviceOffering.active());
        entity.setAvailabilityRule(serviceOffering.availabilityRule());
        return entity;
    }
}
