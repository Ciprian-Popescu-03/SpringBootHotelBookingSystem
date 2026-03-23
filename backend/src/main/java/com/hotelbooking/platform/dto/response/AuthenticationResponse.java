package com.hotelbooking.platform.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String token) {}
