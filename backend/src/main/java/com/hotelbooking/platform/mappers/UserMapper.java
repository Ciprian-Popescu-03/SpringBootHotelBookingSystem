package com.hotelbooking.platform.mappers;

import com.hotelbooking.platform.dto.request.CreateUserRequest;
import com.hotelbooking.platform.dto.request.RegisterRequest;
import com.hotelbooking.platform.dto.response.UserProfileResponse;
import com.hotelbooking.platform.dto.response.UserResponse;
import com.hotelbooking.platform.entities.AppUser;
import com.hotelbooking.platform.entities.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AppUser toEntity(CreateUserRequest request, String encodedPassword, Role role) {
        return AppUser.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(encodedPassword)
                .role(role)
                .build();
    }

    public AppUser toEntity(RegisterRequest request, String encodedPassword, Role role) {
        return AppUser.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(encodedPassword)
                .role(role)
                .build();
    }

    public UserResponse toUserResponse(AppUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserProfileResponse toUserProfileResponse(AppUser user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
