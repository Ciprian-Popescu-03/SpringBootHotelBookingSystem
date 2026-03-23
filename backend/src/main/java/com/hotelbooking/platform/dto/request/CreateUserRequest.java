package com.hotelbooking.platform.dto.request;

import com.hotelbooking.platform.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateUserRequest(
        @NotBlank
        @Size(max = 100)
        String firstname,
        @NotBlank
        @Size(max = 100)
        String lastname,
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
        String password,
        Role role
) {}
