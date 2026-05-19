package com.hotel.management.mapper;

import com.hotel.management.api.dto.RoomOperationResponse;
import com.hotel.management.api.dto.UpdateRoomStatusRequest;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.room.RoomOperationResult;
import com.hotel.management.service.room.UpdateRoomStatusCommand;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public UpdateRoomStatusCommand toCommand(Long roomId, UpdateRoomStatusRequest request) {
        return new UpdateRoomStatusCommand(roomId, RoomStatus.valueOf(request.getStatus().getValue()));
    }

    public RoomOperationResponse toResponse(RoomOperationResult result) {
        return new RoomOperationResponse()
                .roomId(result.roomId())
                .hotelId(result.hotelId())
                .roomNumber(result.roomNumber())
                .roomTypeId(result.roomTypeId())
                .capacity(result.capacity())
                .status(RoomOperationResponse.StatusEnum.fromValue(result.status()));
    }
}