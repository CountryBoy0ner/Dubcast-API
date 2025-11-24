package com.Tsimur.Dubcast.dto;


import lombok.*;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String role;

}
