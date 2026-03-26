package com.hotelbooking.platform.services;

import com.hotelbooking.platform.dto.request.CreateUserRequest;
import com.hotelbooking.platform.dto.request.ChangePasswordRequest;
import com.hotelbooking.platform.dto.request.UpdateProfileRequest;
import com.hotelbooking.platform.dto.request.UpdateUserRequest;
import com.hotelbooking.platform.dto.request.UpdateUserRoleRequest;
import com.hotelbooking.platform.dto.response.UserProfileResponse;
import com.hotelbooking.platform.dto.response.UserResponse;
import com.hotelbooking.platform.entities.AppUser;
import com.hotelbooking.platform.entities.Role;
import com.hotelbooking.platform.mappers.UserMapper;
import com.hotelbooking.platform.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserProfileResponse getCurrentUserProfile(String email) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(String email, UpdateProfileRequest request) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        applyProfileUpdate(user, request);
        try {
            var saved = repository.save(user);
            return userMapper.toUserProfileResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use", ex);
        }
    }

    @Transactional
    public void changeMyPassword(String email, ChangePasswordRequest request) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is invalid");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        ensureEmailAvailable(request.email(), null);

        var role = request.role() != null ? request.role() : Role.CUSTOMER;
        var encodedPassword = passwordEncoder.encode(request.password());
        var user = userMapper.toEntity(request, encodedPassword, role);

        try {
            var saved = repository.save(user);
            return userMapper.toUserResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use", ex);
        }
    }

    public List<UserResponse> getAllUsers() {
        return repository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return userMapper.toUserResponse(findById(id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        var user = findById(id);
        applyUserUpdate(user, request);
        try {
            var saved = repository.save(user);
            return userMapper.toUserResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use", ex);
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        var user = findById(id);
        repository.delete(user);
    }

    @Transactional
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request) {
        if (request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }

        var user = findById(id);
        user.setRole(request.role());
        repository.save(user);
        return userMapper.toUserResponse(user);
    }

    private AppUser findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void applyUserUpdate(AppUser user, UpdateUserRequest request) {
        applyUserFieldsUpdate(
                user,
                request,
                UpdateUserRequest::firstname,
                UpdateUserRequest::lastname,
                UpdateUserRequest::email,
                UpdateUserRequest::password
        );
    }

    private void applyProfileUpdate(AppUser user, UpdateProfileRequest request) {
        applyUserFieldsUpdate(
                user,
                request,
                UpdateProfileRequest::firstname,
                UpdateProfileRequest::lastname,
                UpdateProfileRequest::email,
                UpdateProfileRequest::password
        );
    }

    private <T> void applyUserFieldsUpdate(
            AppUser user,
            T request,
            Function<T, String> firstnameGetter,
            Function<T, String> lastnameGetter,
            Function<T, String> emailGetter,
            Function<T, String> passwordGetter
    ) {
        String firstname = firstnameGetter.apply(request);
        if (firstname != null) {
            user.setFirstname(firstname);
        }

        String lastname = lastnameGetter.apply(request);
        if (lastname != null) {
            user.setLastname(lastname);
        }

        String email = emailGetter.apply(request);
        if (email != null) {
            if (isBlank(email)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be blank");
            }
            ensureEmailAvailable(email, user.getId());
            user.setEmail(email);
        }

        String password = passwordGetter.apply(request);
        if (password != null) {
            if (isBlank(password)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be blank");
            }
            user.setPassword(passwordEncoder.encode(password));
        }
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        repository.findByEmail(email).ifPresent(existingUser -> {
            if (currentUserId == null || !existingUser.getId().equals(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
            }
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
