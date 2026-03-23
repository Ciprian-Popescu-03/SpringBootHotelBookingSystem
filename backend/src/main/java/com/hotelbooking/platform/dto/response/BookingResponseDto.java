package com.hotelbooking.platform.dto.response;

import com.hotelbooking.platform.enums.BookingStatus;
import java.time.LocalDate;

public record BookingResponseDto(
        Long id,
        Long userId,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status
) {
}
