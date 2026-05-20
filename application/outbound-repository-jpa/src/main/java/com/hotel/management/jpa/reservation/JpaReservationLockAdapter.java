package com.hotel.management.jpa.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.management.domain.reservation.ReservationPriceSnapshot;
import com.hotel.management.domain.reservation.ReservationLockPort;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.EmailAddress;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.domain.shared.value.StayingGuest;
import com.hotel.management.jpa.shared.JsonColumnCodec;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Component
public class JpaReservationLockAdapter implements ReservationLockPort {

    private final JpaReservationSpringDataRepository springDataReservationRepository;

    public JpaReservationLockAdapter(JpaReservationSpringDataRepository springDataReservationRepository) {
        this.springDataReservationRepository = springDataReservationRepository;
    }

    @Override
    public Optional<Reservation> findReservationForChange(String reservationId) {
        return springDataReservationRepository.findByIdForUpdate(reservationId)
                .map(this::toDomain);
    }

    private Reservation toDomain(JpaReservationEntity entity) {
        return Reservation.rehydrate(
                entity.getId(),
                entity.getHotelId(),
                entity.getGuestId(),
                entity.getRoomId(),
                entity.getRoomTypeId(),
                entity.getCheckIn(),
                entity.getCheckOut(),
                new AccommodationParty(
                        new GuestComposition(
                                entity.getAdultsCount(),
                                JsonColumnCodec.read(entity.getChildrenAgesJson(), new TypeReference<List<Integer>>() { }, List.of())
                        ),
                        JsonColumnCodec.read(entity.getPetsJson(), new TypeReference<List<PetDetails>>() { }, List.of()),
                        JsonColumnCodec.read(entity.getStayingGuestsJson(), new TypeReference<List<StayingGuest>>() { }, List.of())
                ),
                toEmailAddress(entity.getContactEmail()),
                entity.getContactPhone(),
                entity.getSpecialRequests(),
                new ReservationPriceSnapshot(
                        new Money(entity.getBasePriceAmount(), Currency.getInstance(entity.getBasePriceCurrency())),
                        new Money(entity.getServicesPriceAmount(), Currency.getInstance(entity.getServicesPriceCurrency())),
                        new Money(entity.getDiscountAmount(), Currency.getInstance(entity.getDiscountCurrency())),
                        new Money(entity.getFinalPriceAmount(), Currency.getInstance(entity.getFinalPriceCurrency()))
                ),
                List.of(),
                ReservationStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getCancelledAt(),
                entity.getCreatedBy()
        );
    }

    private EmailAddress toEmailAddress(String value) {
        return value == null || value.isBlank() ? null : new EmailAddress(value);
    }
}
