package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
  @Email @NotBlank private String email;

  @NotBlank private String role;

  @NotBlank private String password;
}
