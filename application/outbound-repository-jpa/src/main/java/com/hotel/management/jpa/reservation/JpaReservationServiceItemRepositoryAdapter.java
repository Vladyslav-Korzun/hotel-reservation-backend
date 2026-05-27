package com.hotel.management.jpa.reservation;

import com.hotel.management.domain.reservation.ReservationServiceItem;
import com.hotel.management.domain.reservation.ReservationServiceItemRepository;
import com.hotel.management.domain.shared.value.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;

@Component
public class JpaReservationServiceItemRepositoryAdapter implements ReservationServiceItemRepository {

    private final JpaReservationServiceItemSpringDataRepository repository;

    JpaReservationServiceItemRepositoryAdapter(JpaReservationServiceItemSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ReservationServiceItem> findByReservationId(String reservationId) {
        return repository.findByReservationId(reservationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void replaceForReservation(String reservationId, List<ReservationServiceItem> serviceItems) {
        repository.deleteByReservationId(reservationId);
        repository.saveAll(serviceItems.stream()
                .map(this::toEntity)
                .toList());
    }

    private ReservationServiceItem toDomain(JpaReservationServiceItemEntity entity) {
        return new ReservationServiceItem(
                entity.getId(),
                entity.getReservationId(),
                entity.getServiceOfferingId(),
                entity.getServiceNameSnapshot(),
                new Money(entity.getPriceAmount(), Currency.getInstance(entity.getPriceCurrency())),
                entity.getQuantity()
        );
    }

    private JpaReservationServiceItemEntity toEntity(ReservationServiceItem item) {
        var entity = new JpaReservationServiceItemEntity();
        entity.setId(item.id());
        entity.setReservationId(item.reservationId());
        entity.setServiceOfferingId(item.serviceOfferingId());
        entity.setServiceNameSnapshot(item.serviceNameSnapshot());
        entity.setPriceAmount(item.priceSnapshot().amount());
        entity.setPriceCurrency(item.priceSnapshot().currency().getCurrencyCode());
        entity.setQuantity(item.quantity());
        return entity;
    }
}
