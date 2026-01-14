package com.Tsimur.Dubcast.exception.handler;

import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
      throws IOException {
    res.setStatus(HttpStatus.FORBIDDEN.value());
    res.setContentType("application/json");

    var body = ErrorResponse.of(403, "Forbidden", "Forbidden", req.getRequestURI());

    objectMapper.writeValue(res.getWriter(), body);
  }
}
