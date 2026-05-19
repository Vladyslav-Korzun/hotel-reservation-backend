package com.hotel.management.service.hotel;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;
import com.hotel.management.domain.room.RoomResult;
import com.hotel.management.domain.room.RoomTypeResult;

public class HotelQueryResultMapper {

    public HotelResult toResult(Hotel hotel) {
        return new HotelResult(
                hotel.id(),
                hotel.name(),
                hotel.city(),
                hotel.country(),
                hotel.address(),
                hotel.stars(),
                hotel.description(),
                hotel.status().name(),
                hotel.policy().childrenAllowed(),
                hotel.policy().petsAllowed(),
                hotel.policy().infantMaxAge(),
                hotel.policy().childMaxAge(),
                hotel.policy().adultEquivalentAge()
        );
    }

    public HotelServiceOfferingResult toResult(ServiceOffering serviceOffering) {
        return new HotelServiceOfferingResult(
                serviceOffering.id(),
                serviceOffering.hotelId(),
                serviceOffering.code(),
                serviceOffering.name(),
                serviceOffering.description(),
                serviceOffering.price(),
                serviceOffering.active(),
                serviceOffering.availabilityRule()
        );
    }

    public RoomTypeResult toResult(RoomType roomType) {
        return new RoomTypeResult(
                roomType.id(),
                roomType.hotelId(),
                roomType.name(),
                roomType.occupancyPolicy().maxAdults(),
                roomType.occupancyPolicy().maxChildren(),
                roomType.occupancyPolicy().maxInfants(),
                roomType.occupancyPolicy().maxTotalGuests(),
                roomType.petPolicy().petsAllowed(),
                roomType.petPolicy().maxPets(),
                roomType.petPolicy().allowedPetTypes(),
                roomType.petPolicy().maxPetWeightKg(),
                roomType.petPolicy().petFee(),
                roomType.basePrice(),
                roomType.description(),
                roomType.features().bedSetup(),
                roomType.features().roomSizeSqm(),
                roomType.features().amenities()
        );
    }

    public RoomResult toResult(Room room) {
        return new RoomResult(
                room.id(),
                room.hotelId(),
                room.number(),
                room.roomTypeId(),
                room.capacity(),
                room.status().name()
        );
    }
}