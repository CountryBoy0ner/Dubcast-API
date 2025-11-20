package com.Tsimur.Dubcast.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateTokenResponse {
    @NotBlank
    private boolean valid;

    @NotBlank
    private String email;

    @NotBlank
    private String role;
}

