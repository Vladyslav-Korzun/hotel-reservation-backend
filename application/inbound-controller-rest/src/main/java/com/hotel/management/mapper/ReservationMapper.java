package com.hotel.management.mapper;

import com.hotel.management.api.dto.BookingPet;
import com.hotel.management.api.dto.CreateReservationRequest;
import com.hotel.management.api.dto.CreateReservationResponse;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.domain.shared.value.PetSize;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.service.reservation.CreateReservationCommand;
import com.hotel.management.service.reservation.CreateReservationResult;
import com.hotel.management.service.reservation.GetReservationResult;
import com.hotel.management.service.staff.StaffReservationResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "accommodationParty", source = ".")
    CreateReservationCommand toCommand(CreateReservationRequest request);

    CreateReservationResponse toResponse(CreateReservationResult result);

    ReservationResponse toResponse(GetReservationResult result);

    ReservationResponse toResponse(StaffReservationResult result);

    List<ReservationResponse> toResponse(List<GetReservationResult> result);

    default AccommodationParty map(CreateReservationRequest value) {
        if (value == null) {
            return null;
        }
        return new AccommodationParty(
                new GuestComposition(value.getAdults(), value.getChildrenAges()),
                mapPets(value.getPets())
        );
    }

    default List<PetDetails> mapPets(List<BookingPet> value) {
        if (value == null) {
            return List.of();
        }
        return value.stream()
                .map(pet -> new PetDetails(
                        PetType.valueOf(pet.getType().getValue()),
                        PetSize.valueOf(pet.getSize().getValue()),
                        pet.getWeightKg()
                ))
                .toList();
    }

    default List<BookingPet> map(List<PetDetails> value) {
        if (value == null) {
            return List.of();
        }
        return value.stream()
                .map(pet -> new BookingPet()
                        .type(BookingPet.TypeEnum.fromValue(pet.type().name()))
                        .size(BookingPet.SizeEnum.fromValue(pet.size().name()))
                        .weightKg(pet.weightKg()))
                .toList();
    }

    default OffsetDateTime map(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
