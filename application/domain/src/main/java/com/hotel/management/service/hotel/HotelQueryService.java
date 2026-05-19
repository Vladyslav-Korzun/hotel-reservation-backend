package com.hotel.management.service.hotel;

import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;

import java.util.List;
import com.hotel.management.domain.hotel.HotelResult;
import com.hotel.management.domain.hotel.HotelServiceOfferingResult;

public class HotelQueryService implements HotelQueryFacade {

    private final HotelRepository hotelRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final HotelQueryResultMapper hotelQueryResultMapper;

    public HotelQueryService(
            HotelRepository hotelRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            HotelQueryResultMapper hotelQueryResultMapper
    ) {
        this.hotelRepository = hotelRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.hotelQueryResultMapper = hotelQueryResultMapper;
    }

    @Override
    public List<HotelResult> listHotels(ListHotelsQuery query) {
        var city = query == null ? null : query.city();
        var hotels = city == null || city.isBlank()
                ? hotelRepository.findAllActive()
                : hotelRepository.findActiveByCity(city.trim());
        return hotels.stream()
                .map(hotelQueryResultMapper::toResult)
                .toList();
    }

    @Override
    public HotelResult getHotelDetails(Long hotelId) {
        return hotelQueryResultMapper.toResult(loadActiveHotel(hotelId));
    }

    @Override
    public List<HotelServiceOfferingResult> listHotelServices(Long hotelId) {
        var hotel = loadActiveHotel(hotelId);
        return serviceOfferingRepository.findActiveByHotelId(hotel.id()).stream()
                .map(hotelQueryResultMapper::toResult)
                .toList();
    }

    private Hotel loadActiveHotel(Long hotelId) {
        if (hotelId == null) {
            throw new ValidationException("hotelId is required");
        }
        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + hotelId));
        if (!hotel.isActive()) {
            throw new NotFoundException("Hotel not found: " + hotelId);
        }
        return hotel;
    }
}