package com.Tsimur.Dubcast.exception.handler.api;

import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
      throws IOException {
    res.setStatus(HttpStatus.UNAUTHORIZED.value());
    res.setContentType("application/json");

    var body = ErrorResponse.of(401, "Unauthorized", "Unauthorized", req.getRequestURI());

    objectMapper.writeValue(res.getWriter(), body);
  }
}
