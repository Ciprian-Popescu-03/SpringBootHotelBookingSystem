package com.hotelbooking.platform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record RoomRequestDto(
        @NotBlank(message = "Room number cannot be blank")
        String roomNumber,
        @NotBlank(message = "Room type cannot be blank")
        String roomType,
        @Positive(message = "Price per night must be greater than zero")
        double pricePerNight,
        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity,
        @NotNull(message = "Features list cannot be null (but it can be empty)")
        Set<FeatureRequestDto> features
) {
}
