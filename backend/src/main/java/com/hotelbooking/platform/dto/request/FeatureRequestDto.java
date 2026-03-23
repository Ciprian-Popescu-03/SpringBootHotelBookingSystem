package com.hotelbooking.platform.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FeatureRequestDto(
        @NotBlank
        String name
) {
}
