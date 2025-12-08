package com.Tsimur.Dubcast.service.impl;


import com.Tsimur.Dubcast.dto.request.LoginRequest;
import com.Tsimur.Dubcast.dto.request.RegisterRequest;
import com.Tsimur.Dubcast.dto.request.ValidateTokenRequest;
import com.Tsimur.Dubcast.dto.response.AuthResponse;
import com.Tsimur.Dubcast.dto.response.ValidateTokenResponse;
import com.Tsimur.Dubcast.exception.type.EmailAlreadyUsedException;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.model.Role;
import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.security.jwt.JwtService;
import com.Tsimur.Dubcast.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.Tsimur.Dubcast.repository.UserRepository;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;


@Transactional
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }


        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
    @Transactional
    @Override
    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        String token = request.getToken();

        boolean valid = false;
        String email = null;
        String role = null;

        try {
            boolean expired = jwtService.isExpired(token);

            if (!expired) {
                email = jwtService.extractEmail(token);
                role = jwtService.extractRole(token);
                valid = true;
            }
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            valid = false;
        } catch (io.jsonwebtoken.JwtException ex) {
            valid = false;
        }

        return ValidateTokenResponse.builder()
                .valid(valid)
                .email(email)
                .role(role)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("Email already used");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
