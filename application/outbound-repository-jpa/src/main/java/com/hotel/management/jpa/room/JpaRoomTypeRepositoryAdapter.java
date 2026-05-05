package com.hotel.management.jpa.room;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.jpa.shared.JsonColumnCodec;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Set;

@Component
public class JpaRoomTypeRepositoryAdapter implements RoomTypeRepository {

    private final JpaRoomTypeSpringDataRepository roomTypeSpringDataRepository;

    public JpaRoomTypeRepositoryAdapter(JpaRoomTypeSpringDataRepository roomTypeSpringDataRepository) {
        this.roomTypeSpringDataRepository = roomTypeSpringDataRepository;
    }

    @Override
    public List<RoomType> findByHotelIds(List<Long> hotelIds) {
        if (hotelIds == null || hotelIds.isEmpty()) {
            return List.of();
        }

        return roomTypeSpringDataRepository.findByHotelIdIn(hotelIds).stream()
                .map(this::toDomain)
                .toList();
    }

    private RoomType toDomain(JpaRoomTypeEntity entity) {
        return new RoomType(
                entity.getId(),
                entity.getHotelId(),
                entity.getName(),
                new OccupancyPolicy(
                        entity.getMaxAdults(),
                        entity.getMaxChildren(),
                        entity.getMaxInfants(),
                        entity.getMaxTotalGuests()
                ),
                new PetPolicy(
                        Boolean.TRUE.equals(entity.getPetsAllowed()),
                        entity.getMaxPets(),
                        JsonColumnCodec.read(entity.getAllowedPetTypesJson(), new TypeReference<Set<PetType>>() { }, Set.of()),
                        entity.getMaxPetWeightKg(),
                        entity.getPetFeeAmount() == null || entity.getPetFeeCurrency() == null
                                ? null
                                : new Money(entity.getPetFeeAmount(), Currency.getInstance(entity.getPetFeeCurrency()))
                ),
                new Money(entity.getBasePriceAmount(), Currency.getInstance(entity.getBasePriceCurrency())),
                entity.getDescription()
        );
    }
}
