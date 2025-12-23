package com.Tsimur.Dubcast.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

  private UUID id;

  @Email @NotBlank private String email;

  @NotBlank private String role;

  @NotBlank private String username;

  private String bio;
}
