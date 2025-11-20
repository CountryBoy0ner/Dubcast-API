package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.request.LoginRequest;
import com.Tsimur.Dubcast.dto.request.RegisterRequest;
import com.Tsimur.Dubcast.dto.request.ValidateTokenRequest;
import com.Tsimur.Dubcast.dto.response.ValidateTokenResponse;

public interface AuthService {
    public String login(LoginRequest request);

    ValidateTokenResponse validateToken(ValidateTokenRequest request);

    public String register(RegisterRequest request);
}
