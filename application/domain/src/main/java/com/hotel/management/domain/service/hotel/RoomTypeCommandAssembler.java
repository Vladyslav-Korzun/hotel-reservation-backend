package com.hotel.management.domain.service.hotel;

import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.value.Money;

import java.math.BigDecimal;

final class RoomTypeCommandAssembler {

    private RoomTypeCommandAssembler() {
    }

    static OccupancyPolicy toOccupancyPolicy(CreateRoomTypeCommand command) {
        return new OccupancyPolicy(command.maxAdults(), command.maxChildren(), command.maxInfants(), command.maxTotalGuests());
    }

    static OccupancyPolicy toOccupancyPolicy(UpdateRoomTypeCommand command) {
        return new OccupancyPolicy(command.maxAdults(), command.maxChildren(), command.maxInfants(), command.maxTotalGuests());
    }

    static PetPolicy toPetPolicy(CreateRoomTypeCommand command) {
        return new PetPolicy(
                command.petsAllowed(),
                command.maxPets(),
                command.allowedPetTypes(),
                command.maxPetWeightKg(),
                toOptionalMoney(command.petFeeAmount(), command.petFeeCurrency())
        );
    }

    static PetPolicy toPetPolicy(UpdateRoomTypeCommand command) {
        return new PetPolicy(
                command.petsAllowed(),
                command.maxPets(),
                command.allowedPetTypes(),
                command.maxPetWeightKg(),
                toOptionalMoney(command.petFeeAmount(), command.petFeeCurrency())
        );
    }

    static Money toBasePrice(CreateRoomTypeCommand command) {
        return Money.of(command.basePriceAmount(), command.basePriceCurrency());
    }

    static Money toBasePrice(UpdateRoomTypeCommand command) {
        return Money.of(command.basePriceAmount(), command.basePriceCurrency());
    }

    static RoomTypeFeatures toFeatures(CreateRoomTypeCommand command) {
        return new RoomTypeFeatures(command.bedSetup(), command.roomSizeSqm(), command.amenities());
    }

    static RoomTypeFeatures toFeatures(UpdateRoomTypeCommand command) {
        return new RoomTypeFeatures(command.bedSetup(), command.roomSizeSqm(), command.amenities());
    }

    private static Money toOptionalMoney(BigDecimal amount, String currencyCode) {
        if (amount == null && (currencyCode == null || currencyCode.isBlank())) {
            return null;
        }
        if (amount == null) {
            throw new ValidationException("petFee amount is required");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new ValidationException("petFee currency is required");
        }
        return Money.of(amount, currencyCode);
    }
}
