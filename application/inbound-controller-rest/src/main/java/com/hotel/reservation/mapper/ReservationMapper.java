package com.hotel.reservation.mapper;

import com.hotel.reservation.api.dto.CreateReservationRequest;
import com.hotel.reservation.api.dto.CreateReservationResponse;
import com.hotel.reservation.api.dto.ReservationResponse;
import com.hotel.reservation.reservation.service.CreateReservationCommand;
import com.hotel.reservation.reservation.service.CreateReservationResult;
import com.hotel.reservation.reservation.service.GetReservationResult;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    CreateReservationCommand toCommand(CreateReservationRequest request);

    CreateReservationResponse toResponse(CreateReservationResult result);

    ReservationResponse toResponse(GetReservationResult result);

    default OffsetDateTime map(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
