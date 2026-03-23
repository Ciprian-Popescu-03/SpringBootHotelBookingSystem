package com.hotelbooking.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateProfileRequest(
        @Size(max = 100)
        @Pattern(regexp = "^(?!\\s*$).+", message = "firstname cannot be blank")
        String firstname,
        @Size(max = 100)
        @Pattern(regexp = "^(?!\\s*$).+", message = "lastname cannot be blank")
        String lastname,
        @Email
        @Size(max = 255)
        @Pattern(regexp = "^(?!\\s*$).+", message = "email cannot be blank")
        String email,
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
        String password
) {}
