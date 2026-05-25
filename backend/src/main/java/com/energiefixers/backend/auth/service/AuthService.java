package com.energiefixers.backend.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.energiefixers.backend.auth.dto.LoginRequest;
import com.energiefixers.backend.auth.dto.LoginResponse;
import com.energiefixers.backend.user.dto.UserResponse;
import com.energiefixers.backend.user.models.User;
import com.energiefixers.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials."));

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials.");
        }

        var now = java.time.LocalDateTime.now();

        if (user.getLastLoginAt() == null ||
                user.getLastLoginAt().isBefore(now.minusHours(6))) {
            user.setLastLoginAt(now);
            userRepository.save(user);
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail().toLowerCase(), user.getRole().name());
        return new LoginResponse(token, UserResponse.from(user));
    }

    
   

    
}
