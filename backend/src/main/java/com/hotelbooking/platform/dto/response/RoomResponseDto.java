package com.hotelbooking.platform.dto.response;

import java.util.Set;

public record RoomResponseDto(
        Long id,
        String roomNumber,
        String roomType,
        double pricePerNight,
        int capacity,
        Set<FeatureResponseDto> features
) {
}
