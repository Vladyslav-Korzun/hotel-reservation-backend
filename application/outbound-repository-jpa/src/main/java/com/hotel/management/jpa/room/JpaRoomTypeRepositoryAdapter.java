package com.hotel.management.jpa.room;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.jpa.shared.JsonColumnCodec;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class JpaRoomTypeRepositoryAdapter implements RoomTypeRepository {

    private final JpaRoomTypeSpringDataRepository roomTypeSpringDataRepository;

    JpaRoomTypeRepositoryAdapter(JpaRoomTypeSpringDataRepository roomTypeSpringDataRepository) {
        this.roomTypeSpringDataRepository = roomTypeSpringDataRepository;
    }

    @Override
    public RoomType save(RoomType roomType) {
        return toDomain(roomTypeSpringDataRepository.save(toEntity(roomType)));
    }

    @Override
    public Optional<RoomType> findById(Long roomTypeId) {
        return roomTypeSpringDataRepository.findById(roomTypeId)
                .map(this::toDomain);
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
                entity.getDescription(),
                new RoomTypeFeatures(
                        entity.getBedSetup(),
                        entity.getRoomSizeSqm(),
                        JsonColumnCodec.read(entity.getAmenitiesJson(), new TypeReference<Set<RoomAmenity>>() { }, Set.of())
                )
        );
    }

    private JpaRoomTypeEntity toEntity(RoomType roomType) {
        var entity = new JpaRoomTypeEntity();
        entity.setId(roomType.id());
        entity.setHotelId(roomType.hotelId());
        entity.setName(roomType.name());
        entity.setMaxAdults(roomType.occupancyPolicy().maxAdults());
        entity.setMaxChildren(roomType.occupancyPolicy().maxChildren());
        entity.setMaxInfants(roomType.occupancyPolicy().maxInfants());
        entity.setMaxTotalGuests(roomType.occupancyPolicy().maxTotalGuests());
        entity.setPetsAllowed(roomType.petPolicy().petsAllowed());
        entity.setMaxPets(roomType.petPolicy().maxPets());
        entity.setAllowedPetTypesJson(JsonColumnCodec.write(roomType.petPolicy().allowedPetTypes()));
        entity.setMaxPetWeightKg(roomType.petPolicy().maxPetWeightKg());
        entity.setPetFeeAmount(roomType.petPolicy().petFee() == null ? null : roomType.petPolicy().petFee().amount());
        entity.setPetFeeCurrency(roomType.petPolicy().petFee() == null
                ? null
                : roomType.petPolicy().petFee().currency().getCurrencyCode());
        entity.setBasePriceAmount(roomType.basePrice().amount());
        entity.setBasePriceCurrency(roomType.basePrice().currency().getCurrencyCode());
        entity.setDescription(roomType.description());
        entity.setBedSetup(roomType.features().bedSetup());
        entity.setRoomSizeSqm(roomType.features().roomSizeSqm());
        entity.setAmenitiesJson(JsonColumnCodec.write(roomType.features().amenities()));
        return entity;
    }
}
