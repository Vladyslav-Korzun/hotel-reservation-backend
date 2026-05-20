package com.hotel.management.jpa.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationPriceSnapshot;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.reservation.ReservationServiceItemRepository;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.EmailAddress;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.jpa.shared.JsonColumnCodec;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Component
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationSpringDataRepository springDataReservationRepository;
    private final ReservationServiceItemRepository reservationServiceItemRepository;

    public JpaReservationRepositoryAdapter(
            JpaReservationSpringDataRepository springDataReservationRepository,
            ReservationServiceItemRepository reservationServiceItemRepository
    ) {
        this.springDataReservationRepository = springDataReservationRepository;
        this.reservationServiceItemRepository = reservationServiceItemRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        var entity = new JpaReservationEntity();
        entity.setId(reservation.id());
        entity.setHotelId(reservation.hotelId());
        entity.setGuestId(reservation.guestId());
        entity.setRoomId(reservation.roomId());
        entity.setRoomTypeId(reservation.roomTypeId());
        entity.setCheckIn(reservation.checkIn());
        entity.setCheckOut(reservation.checkOut());
        entity.setAdultsCount(reservation.accommodationParty().guests().adults());
        entity.setChildrenAgesJson(JsonColumnCodec.write(reservation.accommodationParty().guests().childrenAges()));
        entity.setPetsJson(JsonColumnCodec.write(reservation.accommodationParty().pets()));
        entity.setContactEmail(reservation.contactEmail() == null ? null : reservation.contactEmail().value());
        entity.setContactPhone(reservation.contactPhone());
        entity.setSpecialRequests(reservation.specialRequests());
        entity.setBasePriceAmount(reservation.basePrice().amount());
        entity.setBasePriceCurrency(reservation.basePrice().currency().getCurrencyCode());
        entity.setServicesPriceAmount(reservation.servicesPrice().amount());
        entity.setServicesPriceCurrency(reservation.servicesPrice().currency().getCurrencyCode());
        entity.setDiscountAmount(reservation.discountAmount().amount());
        entity.setDiscountCurrency(reservation.discountAmount().currency().getCurrencyCode());
        entity.setFinalPriceAmount(reservation.finalPrice().amount());
        entity.setFinalPriceCurrency(reservation.finalPrice().currency().getCurrencyCode());
        entity.setStatus(reservation.status().name());
        entity.setCreatedAt(reservation.createdAt());
        entity.setCancelledAt(reservation.cancelledAt());
        entity.setCreatedBy(reservation.createdBy());

        var savedEntity = springDataReservationRepository.save(entity);
        reservationServiceItemRepository.replaceForReservation(savedEntity.getId(), reservation.serviceItems());
        return Reservation.rehydrate(
                savedEntity.getId(),
                savedEntity.getHotelId(),
                savedEntity.getGuestId(),
                savedEntity.getRoomId(),
                savedEntity.getRoomTypeId(),
                savedEntity.getCheckIn(),
                savedEntity.getCheckOut(),
                toAccommodationParty(savedEntity),
                toEmailAddress(savedEntity.getContactEmail()),
                savedEntity.getContactPhone(),
                savedEntity.getSpecialRequests(),
                toPriceSnapshot(savedEntity),
                reservationServiceItemRepository.findByReservationId(savedEntity.getId()),
                ReservationStatus.valueOf(savedEntity.getStatus()),
                savedEntity.getCreatedAt(),
                savedEntity.getCancelledAt(),
                savedEntity.getCreatedBy()
        );
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        return springDataReservationRepository.findById(reservationId)
                .map(this::toDomain);
    }

    @Override
    public List<Reservation> findAll(int limit) {
        return springDataReservationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> findByGuestId(Long guestId, int limit) {
        return springDataReservationRepository.findByGuestIdOrderByCreatedAtDesc(guestId, PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    private Reservation toDomain(JpaReservationEntity savedEntity) {
        return Reservation.rehydrate(
            savedEntity.getId(),
            savedEntity.getHotelId(),
            savedEntity.getGuestId(),
            savedEntity.getRoomId(),
            savedEntity.getRoomTypeId(),
                savedEntity.getCheckIn(),
                savedEntity.getCheckOut(),
                toAccommodationParty(savedEntity),
                toEmailAddress(savedEntity.getContactEmail()),
                savedEntity.getContactPhone(),
                savedEntity.getSpecialRequests(),
                toPriceSnapshot(savedEntity),
                reservationServiceItemRepository.findByReservationId(savedEntity.getId()),
                ReservationStatus.valueOf(savedEntity.getStatus()),
                savedEntity.getCreatedAt(),
                savedEntity.getCancelledAt(),
                savedEntity.getCreatedBy()
        );
    }

    private AccommodationParty toAccommodationParty(JpaReservationEntity entity) {
        return new AccommodationParty(
                new GuestComposition(
                        entity.getAdultsCount(),
                        JsonColumnCodec.read(entity.getChildrenAgesJson(), new TypeReference<List<Integer>>() { }, List.of())
                ),
                JsonColumnCodec.read(entity.getPetsJson(), new TypeReference<List<PetDetails>>() { }, List.of())
        );
    }

    private ReservationPriceSnapshot toPriceSnapshot(JpaReservationEntity entity) {
        return new ReservationPriceSnapshot(
                new Money(entity.getBasePriceAmount(), Currency.getInstance(entity.getBasePriceCurrency())),
                new Money(entity.getServicesPriceAmount(), Currency.getInstance(entity.getServicesPriceCurrency())),
                new Money(entity.getDiscountAmount(), Currency.getInstance(entity.getDiscountCurrency())),
                new Money(entity.getFinalPriceAmount(), Currency.getInstance(entity.getFinalPriceCurrency()))
        );
    }

    private EmailAddress toEmailAddress(String value) {
        return value == null || value.isBlank() ? null : new EmailAddress(value);
    }
}
