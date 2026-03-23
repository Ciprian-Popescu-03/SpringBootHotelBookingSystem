package com.hotelbooking.platform.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequestDto(
        @NotNull
        Long roomId,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate
) {
}
