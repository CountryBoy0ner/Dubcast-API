package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.request.LoginRequest;
import com.Tsimur.Dubcast.dto.request.RegisterRequest;
import com.Tsimur.Dubcast.dto.request.ValidateTokenRequest;
import com.Tsimur.Dubcast.dto.response.AuthResponse;
import com.Tsimur.Dubcast.dto.response.ValidateTokenResponse;
import com.Tsimur.Dubcast.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        AuthResponse response = authService .register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response  = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateTokenResponse> validate(
            @RequestBody @Valid ValidateTokenRequest request
    ) {
        ValidateTokenResponse response = authService.validateToken(request);
        return ResponseEntity.ok(response);
    }
}
