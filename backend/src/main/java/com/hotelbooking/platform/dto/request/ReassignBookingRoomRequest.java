package com.hotelbooking.platform.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReassignBookingRoomRequest(
        @NotNull
        Long roomId
) {
}
