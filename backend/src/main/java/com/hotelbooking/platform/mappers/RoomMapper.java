package com.hotelbooking.platform.mappers;

import com.hotelbooking.platform.dto.request.RoomRequestDto;
import com.hotelbooking.platform.dto.response.RoomResponseDto;
import com.hotelbooking.platform.entities.Room;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoomMapper {

    private final FeatureMapper featureMapper;

    public RoomMapper(FeatureMapper featureMapper) {
        this.featureMapper = featureMapper;
    }

    public RoomResponseDto toResponseDto(Room room) {
        return new RoomResponseDto(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPricePerNight(),
                room.getCapacity(),
                room.getFeatures().stream()
                        .map(featureMapper::toResponseDto)
                        .collect(Collectors.toSet())
        );
    }

    public Room toEntity(RoomRequestDto dto) {
        Room room = new Room(
                dto.roomNumber(),
                dto.roomType(),
                dto.pricePerNight(),
                dto.capacity()
        );

        if (dto.features() != null) {
            room.setFeatures(
                    dto.features().stream()
                            .map(featureMapper::toEntity)
                            .collect(Collectors.toSet())
            );
        }

        return room;
    }

    public void updateEntity(Room room, RoomRequestDto dto) {
        room.setRoomNumber(dto.roomNumber());
        room.setRoomType(dto.roomType());
        room.setPricePerNight(dto.pricePerNight());
        room.setCapacity(dto.capacity());
    }
}
