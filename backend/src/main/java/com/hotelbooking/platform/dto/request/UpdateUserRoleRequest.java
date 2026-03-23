package com.hotelbooking.platform.dto.request;

import com.hotelbooking.platform.entities.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateUserRoleRequest(@NotNull Role role) {}
