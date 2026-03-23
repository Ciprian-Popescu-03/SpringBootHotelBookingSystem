package com.hotelbooking.platform.dto.response;

import com.hotelbooking.platform.entities.Role;
import lombok.Builder;

@Builder
public record UserProfileResponse(
        Long id,
        String firstname,
        String lastname,
        String email,
        Role role
) {}
