package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ValidateTokenRequest {

  @NotBlank private String token;
}
