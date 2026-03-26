package com.hotelbooking.platform.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ModifyBookingDatesRequest(
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate
) {
}
