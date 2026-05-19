package com.hotel.management.mapper;

import com.hotel.management.api.dto.BookingPet;
import com.hotel.management.api.dto.CreateReservationRequest;
import com.hotel.management.api.dto.CreateReservationResponse;
import com.hotel.management.api.dto.PublicCreateReservationRequest;
import com.hotel.management.api.dto.ReservationResponse;
import com.hotel.management.api.dto.ReservationServiceItemResponse;
import com.hotel.management.api.dto.ReservationServiceSelection;
import com.hotel.management.api.dto.StaffCreateReservationRequest;
import com.hotel.management.domain.serviceoffering.ServiceOfferingSelection;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.shared.value.PetDetails;
import com.hotel.management.domain.shared.value.PetSize;
import com.hotel.management.domain.shared.value.PetType;
import com.hotel.management.service.reservation.CreatePublicReservationCommand;
import com.hotel.management.service.reservation.CreateReservationCommand;
import com.hotel.management.domain.reservation.CreateReservationResult;
import com.hotel.management.service.reservation.CreateStaffReservationCommand;
import com.hotel.management.domain.reservation.GetReservationResult;
import com.hotel.management.service.reservation.GuestContactCommand;
import com.hotel.management.domain.reservation.ReservationServiceItemResult;
import com.hotel.management.domain.reservation.StaffReservationResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class ReservationMapper {

    public CreateReservationCommand toCommand(CreateReservationRequest request) {
        return new CreateReservationCommand(
                request.getHotelId(),
                request.getRoomTypeId(),
                request.getCheckIn(),
                request.getCheckOut(),
                toAccommodationParty(request.getAdults(), request.getChildrenAges(), request.getPets()),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getSpecialRequests(),
                toServiceOfferingSelections(request.getServiceOfferings())
        );
    }

    public CreatePublicReservationCommand toCommand(PublicCreateReservationRequest request) {
        return new CreatePublicReservationCommand(
                request.getHotelId(),
                request.getRoomTypeId(),
                request.getCheckIn(),
                request.getCheckOut(),
                toAccommodationParty(request.getAdults(), request.getChildrenAges(), request.getPets()),
                toServiceOfferingSelections(request.getServiceOfferings()),
                new GuestContactCommand(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPhone()
                )
        );
    }

    public CreateStaffReservationCommand toCommand(StaffCreateReservationRequest request) {
        return new CreateStaffReservationCommand(
                request.getHotelId(),
                request.getRoomTypeId(),
                request.getGuestId(),
                optionalGuestContact(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPhone()
                ),
                request.getCheckIn(),
                request.getCheckOut(),
                toAccommodationParty(request.getAdults(), request.getChildrenAges(), request.getPets()),
                toServiceOfferingSelections(request.getServiceOfferings())
        );
    }

    public CreateReservationResponse toResponse(CreateReservationResult result) {
        CreateReservationResponse response = new CreateReservationResponse();
        response.reservationId(result.reservationId())
                .hotelId(result.hotelId())
                .guestId(result.guestId())
                .roomId(result.roomId())
                .roomTypeId(result.roomTypeId())
                .checkIn(result.checkIn())
                .checkOut(result.checkOut())
                .adults(result.adults())
                .childrenAges(result.childrenAges())
                .pets(toPets(result.pets()))
                .contactEmail(result.contactEmail())
                .contactPhone(result.contactPhone())
                .specialRequests(result.specialRequests())
                .basePriceAmount(result.basePrice().amount())
                .basePriceCurrency(currency(result.basePrice()))
                .servicesPriceAmount(result.servicesPrice().amount())
                .servicesPriceCurrency(currency(result.servicesPrice()))
                .discountAmount(result.discountAmount().amount())
                .discountCurrency(currency(result.discountAmount()))
                .finalPriceAmount(result.finalPrice().amount())
                .finalPriceCurrency(currency(result.finalPrice()))
                .serviceItems(toServiceItems(result.serviceItems()))
                .status(result.status())
                .createdAt(toOffsetDateTime(result.createdAt()))
                .cancelledAt(toOffsetDateTime(result.cancelledAt()))
                .createdBy(result.createdBy());
        return response;
    }

    public ReservationResponse toResponse(GetReservationResult result) {
        ReservationResponse response = new ReservationResponse();
        copy(result, response);
        return response;
    }

    public ReservationResponse toResponse(StaffReservationResult result) {
        ReservationResponse response = new ReservationResponse();
        copy(result, response);
        return response;
    }

    public List<ReservationResponse> toResponse(List<GetReservationResult> result) {
        return result.stream()
                .map(this::toResponse)
                .toList();
    }

    private void copy(CreateReservationResult result, ReservationResponse response) {
        response.reservationId(result.reservationId())
                .hotelId(result.hotelId())
                .guestId(result.guestId())
                .roomId(result.roomId())
                .roomTypeId(result.roomTypeId())
                .checkIn(result.checkIn())
                .checkOut(result.checkOut())
                .adults(result.adults())
                .childrenAges(result.childrenAges())
                .pets(toPets(result.pets()))
                .contactEmail(result.contactEmail())
                .contactPhone(result.contactPhone())
                .specialRequests(result.specialRequests())
                .basePriceAmount(result.basePrice().amount())
                .basePriceCurrency(currency(result.basePrice()))
                .servicesPriceAmount(result.servicesPrice().amount())
                .servicesPriceCurrency(currency(result.servicesPrice()))
                .discountAmount(result.discountAmount().amount())
                .discountCurrency(currency(result.discountAmount()))
                .finalPriceAmount(result.finalPrice().amount())
                .finalPriceCurrency(currency(result.finalPrice()))
                .serviceItems(toServiceItems(result.serviceItems()))
                .status(result.status())
                .createdAt(toOffsetDateTime(result.createdAt()))
                .cancelledAt(toOffsetDateTime(result.cancelledAt()))
                .createdBy(result.createdBy());
    }

    private void copy(GetReservationResult result, ReservationResponse response) {
        response.reservationId(result.reservationId())
                .hotelId(result.hotelId())
                .guestId(result.guestId())
                .roomId(result.roomId())
                .roomTypeId(result.roomTypeId())
                .checkIn(result.checkIn())
                .checkOut(result.checkOut())
                .adults(result.adults())
                .childrenAges(result.childrenAges())
                .pets(toPets(result.pets()))
                .contactEmail(result.contactEmail())
                .contactPhone(result.contactPhone())
                .specialRequests(result.specialRequests())
                .basePriceAmount(result.basePrice().amount())
                .basePriceCurrency(currency(result.basePrice()))
                .servicesPriceAmount(result.servicesPrice().amount())
                .servicesPriceCurrency(currency(result.servicesPrice()))
                .discountAmount(result.discountAmount().amount())
                .discountCurrency(currency(result.discountAmount()))
                .finalPriceAmount(result.finalPrice().amount())
                .finalPriceCurrency(currency(result.finalPrice()))
                .serviceItems(toServiceItems(result.serviceItems()))
                .status(result.status())
                .createdAt(toOffsetDateTime(result.createdAt()))
                .cancelledAt(toOffsetDateTime(result.cancelledAt()))
                .createdBy(result.createdBy());
    }

    private void copy(StaffReservationResult result, ReservationResponse response) {
        response.reservationId(result.reservationId())
                .hotelId(result.hotelId())
                .guestId(result.guestId())
                .roomId(result.roomId())
                .roomTypeId(result.roomTypeId())
                .checkIn(result.checkIn())
                .checkOut(result.checkOut())
                .adults(result.adults())
                .childrenAges(result.childrenAges())
                .pets(toPets(result.pets()))
                .contactEmail(result.contactEmail())
                .contactPhone(result.contactPhone())
                .specialRequests(result.specialRequests())
                .basePriceAmount(result.basePrice().amount())
                .basePriceCurrency(currency(result.basePrice()))
                .servicesPriceAmount(result.servicesPrice().amount())
                .servicesPriceCurrency(currency(result.servicesPrice()))
                .discountAmount(result.discountAmount().amount())
                .discountCurrency(currency(result.discountAmount()))
                .finalPriceAmount(result.finalPrice().amount())
                .finalPriceCurrency(currency(result.finalPrice()))
                .serviceItems(toServiceItems(result.serviceItems()))
                .status(result.status())
                .createdAt(toOffsetDateTime(result.createdAt()))
                .cancelledAt(toOffsetDateTime(result.cancelledAt()))
                .createdBy(result.createdBy());
    }

    private AccommodationParty toAccommodationParty(int adults, List<Integer> childrenAges, List<BookingPet> pets) {
        return new AccommodationParty(
                new GuestComposition(adults, childrenAges),
                toPetDetails(pets)
        );
    }

    private GuestContactCommand optionalGuestContact(String firstName, String lastName, String email, String phone) {
        if (isBlank(firstName) && isBlank(lastName) && isBlank(email) && isBlank(phone)) {
            return null;
        }
        return new GuestContactCommand(firstName, lastName, email, phone);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<ServiceOfferingSelection> toServiceOfferingSelections(List<ReservationServiceSelection> value) {
        if (value == null) {
            return List.of();
        }
        return value.stream()
                .map(selection -> new ServiceOfferingSelection(selection.getServiceOfferingId(), selection.getQuantity()))
                .toList();
    }

    private List<PetDetails> toPetDetails(List<BookingPet> value) {
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

    private List<BookingPet> toPets(List<PetDetails> value) {
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

    private List<ReservationServiceItemResponse> toServiceItems(List<ReservationServiceItemResult> value) {
        if (value == null) {
            return List.of();
        }
        return value.stream()
                .map(item -> new ReservationServiceItemResponse()
                        .serviceOfferingId(item.serviceOfferingId())
                        .serviceName(item.serviceName())
                        .priceAmount(item.price().amount())
                        .priceCurrency(currency(item.price()))
                        .quantity(item.quantity())
                        .totalPriceAmount(item.totalPrice().amount())
                        .totalPriceCurrency(currency(item.totalPrice())))
                .toList();
    }

    private String currency(Money money) {
        return money.currency().getCurrencyCode();
    }

    private OffsetDateTime toOffsetDateTime(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}