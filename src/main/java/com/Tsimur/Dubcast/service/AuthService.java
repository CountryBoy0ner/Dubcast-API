package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.request.LoginRequest;
import com.Tsimur.Dubcast.dto.request.RegisterRequest;
import com.Tsimur.Dubcast.dto.request.ValidateTokenRequest;
import com.Tsimur.Dubcast.dto.response.AuthResponse;
import com.Tsimur.Dubcast.dto.response.ValidateTokenResponse;

public interface AuthService {
  public AuthResponse login(LoginRequest request);

  public ValidateTokenResponse validateToken(ValidateTokenRequest request);

  public AuthResponse register(RegisterRequest request);
}
