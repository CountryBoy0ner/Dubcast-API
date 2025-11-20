package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public  class UpdateUserRequest {
    @Email
    private String email;

    private String role;
}