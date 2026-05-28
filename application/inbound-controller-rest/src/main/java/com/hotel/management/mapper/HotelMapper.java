package com.hotel.management.mapper;

import com.hotel.management.api.dto.HotelResponse;
import com.hotel.management.api.dto.HotelServiceOfferingResponse;
import com.hotel.management.api.dto.CreateHotelRequest;
import com.hotel.management.api.dto.CreateRoomRequest;
import com.hotel.management.api.dto.CreateRoomTypeRequest;
import com.hotel.management.api.dto.CreateServiceOfferingRequest;
import com.hotel.management.api.dto.RoomAmenityCode;
import com.hotel.management.api.dto.RoomTypeResponse;
import com.hotel.management.api.dto.RoomResponse;
import com.hotel.management.api.dto.UpdateHotelRequest;
import com.hotel.management.api.dto.UpdateRoomRequest;
import com.hotel.management.api.dto.UpdateRoomTypeRequest;
import com.hotel.management.api.dto.UpdateServiceOfferingRequest;
import com.hotel.management.api.dto.AssignStaffToHotelRequest;
import com.hotel.management.api.dto.AuditLogEntryResponse;
import com.hotel.management.api.dto.StaffResponse;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.room.RoomAmenity;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.domain.service.hotel.CreateHotelCommand;
import com.hotel.management.domain.service.hotel.CreateRoomCommand;
import com.hotel.management.domain.service.hotel.CreateRoomTypeCommand;
import com.hotel.management.domain.service.hotel.CreateServiceOfferingCommand;
import com.hotel.management.domain.service.hotel.DeactivateServiceOfferingCommand;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.service.hotel.ListHotelsQuery;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomTypeResult;
import com.hotel.management.domain.service.hotel.UpdateHotelCommand;
import com.hotel.management.domain.service.hotel.UpdateRoomCommand;
import com.hotel.management.domain.service.hotel.UpdateRoomTypeCommand;
import com.hotel.management.domain.service.hotel.UpdateServiceOfferingCommand;
import com.hotel.management.domain.service.staff.AssignStaffToHotelCommand;
import com.hotel.management.domain.audit.AuditLogResult;
import com.hotel.management.domain.staff.StaffResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HotelMapper {

    public ListHotelsQuery toQuery(String city) {
        return new ListHotelsQuery(city);
    }

    public CreateHotelCommand toCommand(CreateHotelRequest request) {
        return new CreateHotelCommand(
                request.getHotelId(),
                request.getName(),
                request.getCity(),
                request.getCountry(),
                request.getAddress(),
                request.getStars(),
                request.getDescription(),
                HotelStatus.valueOf(request.getStatus().getValue()),
                request.getChildrenAllowed(),
                request.getPetsAllowed(),
                request.getInfantMaxAge(),
                request.getChildMaxAge(),
                request.getAdultEquivalentAge()
        );
    }

    public UpdateHotelCommand toCommand(Long hotelId, UpdateHotelRequest request) {
        return new UpdateHotelCommand(
                hotelId,
                request.getName(),
                request.getCity(),
                request.getCountry(),
                request.getAddress(),
                request.getStars(),
                request.getDescription(),
                HotelStatus.valueOf(request.getStatus().getValue()),
                request.getChildrenAllowed(),
                request.getPetsAllowed(),
                request.getInfantMaxAge(),
                request.getChildMaxAge(),
                request.getAdultEquivalentAge()
        );
    }

    public CreateRoomTypeCommand toCommand(CreateRoomTypeRequest request) {
        return new CreateRoomTypeCommand(
                request.getRoomTypeId(),
                request.getHotelId(),
                request.getName(),
                request.getMaxAdults(),
                request.getMaxChildren(),
                request.getMaxInfants(),
                request.getMaxTotalGuests(),
                request.getPetsAllowed(),
                request.getMaxPets(),
                toPetTypes(request.getAllowedPetTypes()),
                request.getMaxPetWeightKg(),
                request.getPetFeeAmount(),
                request.getPetFeeCurrency(),
                request.getBasePriceAmount(),
                request.getBasePriceCurrency(),
                request.getDescription(),
                request.getBedSetup(),
                toBigDecimal(request.getRoomSizeSqm()),
                toAmenities(request.getAmenities())
        );
    }

    public UpdateRoomTypeCommand toCommand(Long roomTypeId, UpdateRoomTypeRequest request) {
        return new UpdateRoomTypeCommand(
                roomTypeId,
                request.getName(),
                request.getMaxAdults(),
                request.getMaxChildren(),
                request.getMaxInfants(),
                request.getMaxTotalGuests(),
                request.getPetsAllowed(),
                request.getMaxPets(),
                toPetTypesFromUpdate(request.getAllowedPetTypes()),
                request.getMaxPetWeightKg(),
                request.getPetFeeAmount(),
                request.getPetFeeCurrency(),
                request.getBasePriceAmount(),
                request.getBasePriceCurrency(),
                request.getDescription(),
                request.getBedSetup(),
                toBigDecimal(request.getRoomSizeSqm()),
                toAmenities(request.getAmenities())
        );
    }

    public CreateRoomCommand toCommand(CreateRoomRequest request) {
        return new CreateRoomCommand(
                request.getRoomId(),
                request.getHotelId(),
                request.getRoomNumber(),
                request.getRoomTypeId(),
                request.getCapacity(),
                RoomStatus.valueOf(request.getStatus().getValue())
        );
    }

    public UpdateRoomCommand toCommand(Long roomId, UpdateRoomRequest request) {
        return new UpdateRoomCommand(
                roomId,
                request.getHotelId(),
                request.getRoomNumber(),
                request.getRoomTypeId(),
                request.getCapacity(),
                RoomStatus.valueOf(request.getStatus().getValue())
        );
    }

    public CreateServiceOfferingCommand toCommand(Long hotelId, CreateServiceOfferingRequest request) {
        return new CreateServiceOfferingCommand(
                hotelId,
                request.getServiceOfferingId(),
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getPriceAmount(),
                request.getPriceCurrency(),
                request.getActive(),
                request.getAvailabilityRule()
        );
    }

    public UpdateServiceOfferingCommand toCommand(
            Long hotelId,
            Long serviceOfferingId,
            UpdateServiceOfferingRequest request
    ) {
        return new UpdateServiceOfferingCommand(
                hotelId,
                serviceOfferingId,
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getPriceAmount(),
                request.getPriceCurrency(),
                request.getActive(),
                request.getAvailabilityRule()
        );
    }

    public DeactivateServiceOfferingCommand toDeactivateServiceOfferingCommand(Long hotelId, Long serviceOfferingId) {
        return new DeactivateServiceOfferingCommand(hotelId, serviceOfferingId);
    }

    public HotelResponse toResponse(HotelResult result) {
        return new HotelResponse()
                .hotelId(result.hotelId())
                .name(result.name())
                .city(result.city())
                .country(result.country())
                .address(result.address())
                .stars(result.stars())
                .description(result.description())
                .status(HotelResponse.StatusEnum.fromValue(result.status()))
                .childrenAllowed(result.childrenAllowed())
                .petsAllowed(result.petsAllowed())
                .infantMaxAge(result.infantMaxAge())
                .childMaxAge(result.childMaxAge())
                .adultEquivalentAge(result.adultEquivalentAge());
    }

    public List<HotelResponse> toHotelResponse(List<HotelResult> results) {
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    public HotelServiceOfferingResponse toResponse(HotelServiceOfferingResult result) {
        return new HotelServiceOfferingResponse()
                .serviceOfferingId(result.serviceOfferingId())
                .hotelId(result.hotelId())
                .code(result.code())
                .name(result.name())
                .description(result.description())
                .priceAmount(result.price().amount())
                .priceCurrency(result.price().currency().getCurrencyCode())
                .active(result.active())
                .availabilityRule(result.availabilityRule());
    }

    public List<HotelServiceOfferingResponse> toServiceOfferingResponse(List<HotelServiceOfferingResult> results) {
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomTypeResponse toResponse(RoomTypeResult result) {
        var response = new RoomTypeResponse()
                .roomTypeId(result.roomTypeId())
                .hotelId(result.hotelId())
                .name(result.name())
                .maxAdults(result.maxAdults())
                .maxChildren(result.maxChildren())
                .maxInfants(result.maxInfants())
                .maxTotalGuests(result.maxTotalGuests())
                .petsAllowed(result.petsAllowed())
                .maxPets(result.maxPets())
                .allowedPetTypes(result.allowedPetTypes().stream()
                        .map(type -> RoomTypeResponse.AllowedPetTypesEnum.fromValue(type.name()))
                        .toList())
                .maxPetWeightKg(result.maxPetWeightKg())
                .basePriceAmount(result.basePrice().amount())
                .basePriceCurrency(result.basePrice().currency().getCurrencyCode())
                .description(result.description())
                .bedSetup(result.bedSetup())
                .roomSizeSqm(toFloat(result.roomSizeSqm()))
                .amenities(toAmenityCodes(result.amenities()));
        if (result.petFee() != null) {
            response.petFeeAmount(result.petFee().amount())
                    .petFeeCurrency(result.petFee().currency().getCurrencyCode());
        }
        return response;
    }

    public List<RoomTypeResponse> toRoomTypeResponse(List<RoomTypeResult> results) {
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomResponse toResponse(RoomResult result) {
        return new RoomResponse()
                .roomId(result.roomId())
                .hotelId(result.hotelId())
                .roomNumber(result.roomNumber())
                .roomTypeId(result.roomTypeId())
                .capacity(result.capacity())
                .status(RoomResponse.StatusEnum.fromValue(result.status()));
    }

    private Set<PetType> toPetTypes(List<CreateRoomTypeRequest.AllowedPetTypesEnum> value) {
        if (value == null) {
            return Set.of();
        }
        return value.stream()
                .map(type -> PetType.valueOf(type.getValue()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<PetType> toPetTypesFromUpdate(List<UpdateRoomTypeRequest.AllowedPetTypesEnum> value) {
        if (value == null) {
            return Set.of();
        }
        return value.stream()
                .map(type -> PetType.valueOf(type.getValue()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<RoomAmenity> toAmenities(List<RoomAmenityCode> value) {
        if (value == null) {
            return Set.of();
        }
        return value.stream()
                .map(code -> RoomAmenity.valueOf(code.getValue()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<RoomAmenityCode> toAmenityCodes(Set<RoomAmenity> value) {
        if (value == null) {
            return List.of();
        }
        return value.stream()
                .sorted()
                .map(amenity -> RoomAmenityCode.fromValue(amenity.name()))
                .toList();
    }

    private BigDecimal toBigDecimal(Float value) {
        return value == null ? null : BigDecimal.valueOf(value.doubleValue());
    }

    private Float toFloat(BigDecimal value) {
        return value == null ? null : value.floatValue();
    }

    public AssignStaffToHotelCommand toAssignStaffCommand(Long staffId, AssignStaffToHotelRequest request) {
        return new AssignStaffToHotelCommand(staffId, request.getHotelId());
    }

    public StaffResponse toStaffResponse(StaffResult result) {
        return new StaffResponse()
                .id(result.id())
                .externalId(result.externalId())
                .hotelId(result.hotelId())
                .username(result.username())
                .email(result.email());
    }

    public List<StaffResponse> toStaffResponseList(List<StaffResult> results) {
        return results.stream()
                .map(this::toStaffResponse)
                .toList();
    }

    public AuditLogEntryResponse toAuditLogEntryResponse(AuditLogResult result) {
        return new AuditLogEntryResponse()
                .id(result.id())
                .actorId(result.actorId())
                .actorRole(result.actorRole())
                .actionType(result.actionType())
                .entityType(result.entityType())
                .entityId(result.entityId())
                .timestamp(result.timestamp().atOffset(java.time.ZoneOffset.UTC))
                .details(result.details());
    }

    public List<AuditLogEntryResponse> toAuditLogEntryResponseList(List<AuditLogResult> results) {
        return results.stream()
                .map(this::toAuditLogEntryResponse)
                .toList();
    }
}