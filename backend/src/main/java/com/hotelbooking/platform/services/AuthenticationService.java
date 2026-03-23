package com.hotelbooking.platform.services;

import com.hotelbooking.platform.dto.request.AuthenticationRequest;
import com.hotelbooking.platform.dto.request.RegisterRequest;
import com.hotelbooking.platform.dto.response.AuthenticationResponse;
import com.hotelbooking.platform.entities.Role;
import com.hotelbooking.platform.mappers.AuthenticationMapper;
import com.hotelbooking.platform.mappers.UserMapper;
import com.hotelbooking.platform.repositories.UserRepository;
import com.hotelbooking.platform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuthenticationMapper authenticationMapper;

    public AuthenticationResponse register(RegisterRequest request) {
        var existingUser = repository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        var encodedPassword = passwordEncoder.encode(request.password());
        var user = userMapper.toEntity(request, encodedPassword, Role.CUSTOMER);
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return authenticationMapper.toAuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var jwtToken = jwtService.generateToken(userDetails);
        return authenticationMapper.toAuthenticationResponse(jwtToken);
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing bearer token");
        }
        String token = authorizationHeader.substring(7);
        jwtService.revokeToken(token);
    }
}
